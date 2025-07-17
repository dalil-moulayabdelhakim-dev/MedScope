package com.dldroid.medscope.manager.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.ui.SplashScreenActivity
import com.google.firebase.database.*

class AppointmentNotificationService : Service() {

    private lateinit var databaseReference: DatabaseReference
    private var accessToken: String? = null

    override fun onCreate() {
        super.onCreate()
        Log.i("NotificationService", "Service Created")

        val sh = applicationContext.getSharedPreferences(AUTH, Context.MODE_PRIVATE)
        accessToken = sh.getString(ACCESS_TOKEN, "")

        if (accessToken.isNullOrEmpty()) {
            Log.e("NotificationService", "No access token found! Stopping service.")
            stopSelf()
            return
        }

        databaseReference = FirebaseDatabase.getInstance().reference.child("Appointments").child(accessToken!!)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("NotificationService", "onStartCommand called")

        startForegroundService()

        // Firebase Listener
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val message = snapshot.child("message").value
                    sendNotification(message)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationService", "Database error: ${error.message}")
            }
        })

        return START_REDELIVER_INTENT
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "Channel2")
            .setContentTitle("Notification Service Running")
            .setSmallIcon(R.drawable.logo_svg)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(2, notification, FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        }else{
            startForeground(2, notification)
        }

    }

    private fun sendNotification(message: Any?) {
        val intent = Intent(this, SplashScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val vibrationPattern = longArrayOf(0, 600, 200, 200, 200, 600)

        val notification = NotificationCompat.Builder(this, "Channel1")
            .setContentTitle("Appointment Notification")
            .setContentText(message.toString())
            .setSmallIcon(R.drawable.logo_svg)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Ensures sound and vibration
            .setSound(notificationSound) // Adds notification sound
            .setVibrate(vibrationPattern) // Adds vibration
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.toString())) // Expandable text
            .setSilent(false) // Ensure it's not muted
            .build()

        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("NotificationService", "Notification permission not granted.")
            return
        }

        notificationManager.notify(1, notification)
        triggerVibration(vibrationPattern)
    }

    @SuppressLint("NewApi")
    private fun triggerVibration(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val vibrator = vibratorManager.defaultVibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }


    override fun onDestroy() {
        Log.i("NotificationService", "Service Destroyed")
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
