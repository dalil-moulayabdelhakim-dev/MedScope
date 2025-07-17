package com.dldroid.medscope.manager

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.FragmentActivity
import com.dldroid.medscope.R
import com.dldroid.medscope.databinding.FragmentHeartBeatMonitoringBinding
import com.dldroid.medscope.ml.EcgModel
import com.dldroid.medscope.ui.dialog.RequestPermissionDialog
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID


class BluetoothManager(
    private val context: Context,
    private val activity: FragmentActivity,
    private val binding: FragmentHeartBeatMonitoringBinding,
    private val series: LineGraphSeries<DataPoint>,
    //private val onEcgDataReceived: (String) -> Unit
) {
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val handler = Handler(Looper.getMainLooper())
    private val updateInterval: Long = 800
    private var graphLastXValue: Double = 0.0
    private val targetDeviceName = "MedScope-BLE"
    private var selectedDevice: BluetoothDevice? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var isReading = false
    private val ecgBuffer = mutableListOf<Float>()
    private val model = EcgModel.newInstance(context)
    private val labels = listOf(
        "Normal Beat",
        "Supraventricular Ectopic Beats",
        "Ventricular Ectopic Beats",
        "Fusion Beats",
        "Unknown Beats"
    )

    private val updateTask = object : Runnable {
        override fun run() {
            checkBluetoothStatus()
            handler.postDelayed(this, updateInterval)
        }
    }

    fun startChecking() {
        Log.d("BluetoothManager", "Starting checking")
        handler.post(updateTask)
    }

    fun stopChecking() {
        Log.d("BluetoothManager", "Stopping checking")
        handler.removeCallbacks(updateTask)
    }

    private fun checkBluetoothStatus() {
        if (PermissionsManager.bluetoothConnectPermission(context)) {
            if (bluetoothAdapter!!.isEnabled) {
                binding.bluetoothEnabledIndicator.setImageResource(R.drawable.ic_check)
                updatePairedDevices()
            } else {
                binding.bluetoothEnabledIndicator.setImageResource(R.drawable.ic_uncheck)
                binding.bluetoothLayout.visibility = View.VISIBLE
                binding.monitorLayout.visibility = View.GONE
            }

            if (bluetoothSocket?.isConnected == true) {
                binding.deviceConnectedIndicator.setImageResource(R.drawable.ic_check)
                binding.monitorLayout.visibility = View.VISIBLE
                binding.bluetoothLayout.visibility = View.GONE
            } else {
                binding.deviceConnectedIndicator.setImageResource(R.drawable.ic_uncheck)
                binding.monitorLayout.visibility = View.GONE
                binding.bluetoothLayout.visibility = View.VISIBLE
            }

        } else {
            RequestPermissionDialog(Manifest.permission.BLUETOOTH_CONNECT).show(
                activity.supportFragmentManager,
                "bluetooth permission"
            )
            RequestPermissionDialog(Manifest.permission.BLUETOOTH_SCAN).show(
                activity.supportFragmentManager,
                "bluetooth permission"
            )
            RequestPermissionDialog(Manifest.permission.ACCESS_FINE_LOCATION).show(
                activity.supportFragmentManager,
                "bluetooth permission"
            )
        }
    }

    private fun updatePairedDevices() {
        val pairedDevices = bluetoothAdapter?.bondedDevices
        val targetDevice = pairedDevices?.find { it.name == targetDeviceName }

        if (targetDevice != null) {
            selectedDevice = targetDevice // ✅ Assign the device object directly
            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                listOf(targetDevice.name)
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.pairedDevicesSpinner.adapter = adapter
        } else {
            selectedDevice = null
            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_item,
                listOf("Device Not Found")
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.pairedDevicesSpinner.adapter = adapter
        }

        binding.connectButton.setOnClickListener { connectToDevice() }
    }

    private fun connectToDevice() {
        if (bluetoothSocket?.isConnected == true) {
            Log.d("BluetoothManager", "Already connected to device. Skipping reconnection.")
            return
        }

        if (bluetoothAdapter?.isEnabled == true) {
            selectedDevice?.let { device ->
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                    bluetoothSocket?.connect()
                    Log.d("BluetoothManager", "Connected to ${device.name}")
                    binding.bluetoothLayout.visibility = View.GONE
                    binding.monitorLayout.visibility = View.VISIBLE
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            "ecg_warning_channel",
                            "ECG Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                        )
                        channel.description = "Alerts for abnormal ECG predictions"

                        val manager = context.getSystemService(NotificationManager::class.java)
                        manager!!.createNotificationChannel(channel)
                    }
                    readBluetoothData()
                } catch (e: IOException) {
                    Log.e("BluetoothManager", "Connection failed, trying insecure mode...")
                    try {
                        bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)
                        bluetoothSocket?.connect()

                        Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
                        Log.d("BluetoothManager", "Connected via insecure mode to ${device.name}")
                        readBluetoothData()
                    } catch (e: IOException) {
                        Log.e("BluetoothManager", "Failed to connect: ${e.message}")
                        bluetoothSocket = null
                        reconnectToDevice()
                    }
                }
            }
        }
    }

    private val executorService = java.util.concurrent.Executors.newSingleThreadExecutor()

    private fun readBluetoothData() {
        executorService.execute {
            try {
                val inputStream = bluetoothSocket?.inputStream
                val dataBuffer = StringBuilder()
                isReading = true

                while (isReading) {
                    val reader = inputStream?.read()
                    if (reader == -1) throw IOException("Bluetooth connection lost") // 🚀 معالجة فقدان الاتصال

                    val charRead = reader?.toChar()
                    if (charRead == ']') {
                        //onEcgDataReceived(dataBuffer.toString())
                        processEcgData(dataBuffer.toString())
                        dataBuffer.setLength(0)
                    } else if (charRead != '[') {
                        dataBuffer.append(charRead)
                    }
                }
            } catch (e: Exception) {
                Log.e("BluetoothManager", "Error in read: ${e.message}")
                //reconnectToDevice() // 🚀 إعادة محاولة الاتصال تلقائيًا
            }
        }
    }

    private fun processEcgData(rawData: String) {
        try {

            val values = rawData.split(", ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (i in values.indices) {
                val ecgValue = values[i].trim { it <= ' ' }.toFloat()
                ecgBuffer.add(ecgValue)
                if (ecgBuffer.size >= 187) {
                    val inputECG = ecgBuffer.take(187).toFloatArray()
                    val predictionIndex = predictECG(inputECG)
                    /*val bpm = getRandomHeartRate(predictionIndex)
                    val builder = java.lang.StringBuilder()

                    for (f in inputECG.indices) {
                        builder.append(inputECG[f])
                        if (f < inputECG.size - 1) {
                            builder.append(", ")
                        }
                    }

                    Log.i("Prediction value2", builder.toString())
                    Log.i("Prediction value2", predictionIndex.toString())*/
                    val colorCodes = intArrayOf(
                        R.color.green_code,
                        R.color.orange_code,
                        R.color.red_code,
                        R.color.yellow_code,
                        R.color.gray_code
                    )
                    activity.runOnUiThread {
                        binding.statusIndex.setBackgroundColor(context.resources.getColor(colorCodes[predictionIndex]))
                        binding.statusText.text = "Normal Beat"

                        ecgBuffer.clear()
                        if (predictionIndex != 0) {
                            sendCriticalEcgNotification()
                            binding.statusText.text = "Abnormal Beat"
                        }
                    }
                }
                activity.runOnUiThread {
                    series.appendData(DataPoint(graphLastXValue, ecgValue.toDouble()), true, 188)
                }
                graphLastXValue += 1.0
                Thread.sleep(5)
            }

        } catch (e: java.lang.Exception) {
            Log.e("ECG", "error in analyse: " + e.message)
        }
    }

    /*fun getRandomHeartRate(aiResult: Int): Int {
        val (min, max) = when (aiResult) {
            0 -> 60 to 100
            1 -> 100 to 160
            2 -> 90 to 170
            3 -> 60 to 110
            4 -> 50 to 120
            else -> 60 to 100
        }
        return (min..max).random()
    }*/

    private fun sendCriticalEcgNotification() {
        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(context, "ecg_warning_channel")
                .setSmallIcon(R.drawable.logo_svg) // use your own icon
                .setContentTitle("ECG Alert")
                //.setContentText("Alert: Patient [Patient test], ID #9376585793, recorded an abnormal ECG signal. Tap to view the sample and full details.")
                .setContentText("Abnormal ECG pattern detected. Please contact your doctor.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(1001, builder.build())
    }

    private fun reconnectToDevice() {
        Log.d("BluetoothManager", "Reconnecting in 3 seconds...")
        handler.postDelayed({
            stopReading() // 🔹 تأكد من إغلاق الاتصال القديم قبل إعادة المحاولة
            connectToDevice()
        }, 3000) // 🔥 إعادة المحاولة بعد 3 ثوانٍ
    }

    fun stopReading() {
        isReading = false
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            Log.d("BluetoothManager", "Bluetooth socket closed")
        } catch (e: IOException) {
            Log.e("BluetoothManager", "Error closing socket: ${e.message}")
        }
    }

    private fun predictECG(ecgData: FloatArray): Int {
        if (ecgData.size != 187) {
            throw IllegalArgumentException("ECG data must have exactly 1x187x1 points")
        }
        //Log.d("ECG_INPUT", "ECG Data: ${ecgData.joinToString()}")
        val byteBuffer =
            ByteBuffer.allocateDirect(187 * java.lang.Float.BYTES).order(ByteOrder.nativeOrder())
        ecgData.forEach { byteBuffer.putFloat(it) }

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 187, 1), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

        val outputs = model.process(inputFeature0)
        val predictions = outputs.outputFeature0AsTensorBuffer.floatArray
        //Log.d("ECG_OUTPUT", "Predictions: ${predictions.joinToString()}")
        return predictions.indices.maxByOrNull { predictions[it] } ?: 0
    }

}
