package com.dldroid.medscope.ui

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.dldroid.medscope.R
import com.dldroid.medscope.database.ACCESS_TOKEN
import com.dldroid.medscope.database.CHECK_CONNECTION_URL
import com.dldroid.medscope.database.AUTH
import com.dldroid.medscope.database.SUCCESS
import org.json.JSONException
import org.json.JSONObject
import kotlin.system.exitProcess

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var noConnection: AlertDialog.Builder
    private var isShowing = false


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.splash_screen_main)
        val appVersion = findViewById<TextView>(R.id.app_version)
        try {
            val prefs = getSharedPreferences(AUTH, MODE_PRIVATE)
            Log.d("DEBUG_PREFS", "Access token after clear: ${prefs.getString(ACCESS_TOKEN, "null")}")
            val version = packageManager
                .getPackageInfo(packageName, 0)
                .versionName
            appVersion.text = "v$version"
        } catch (e: PackageManager.NameNotFoundException) {
            appVersion.text = "vN/A"
        }
        noConnection = AlertDialog.Builder(this@SplashScreenActivity)
        checkConnection()
    }

    @SuppressLint("SwitchIntDef")
    private fun initBiometric() {
        val manager = BiometricManager.from(this)
        when (manager.canAuthenticate()) {
            android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> Toast.makeText(
                applicationContext, getString(R.string.no_fingerPrint), Toast.LENGTH_SHORT
            ).show()

            android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> Toast.makeText(
                applicationContext, getString(R.string.not_working), Toast.LENGTH_SHORT
            ).show()

            android.hardware.biometrics.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.no_fingerPrint_assigned),
                    Toast.LENGTH_SHORT
                ).show()
                val enr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent(Settings.ACTION_BIOMETRIC_ENROLL)
                } else {
                    TODO("VERSION.SDK_INT < R")
                }
                enr.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, DEVICE_POLICY_SERVICE)
                startActivityForResult(enr, 111)
            }
        }
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(Intent(this@SplashScreenActivity, HomeActivity::class.java))
                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

        val promptInfo = PromptInfo.Builder()
            .setTitle(getString(R.string.use_device_password))
            .setDescription(getString(R.string.scan_your_fingerprint))
            .setDeviceCredentialAllowed(true)
            .setConfirmationRequired(true)
            .build()


        biometricPrompt.authenticate(promptInfo)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val channel1 = NotificationChannel("Channel1", "Appointment Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            val channel2 = NotificationChannel("Channel2", "Service Notifications", NotificationManager.IMPORTANCE_LOW)

            manager.createNotificationChannel(channel1)
            manager.createNotificationChannel(channel2)
        }
    }

    private fun checkConnection() {
        val r = StringRequest(
            Request.Method.POST, CHECK_CONNECTION_URL,
            { response ->
                try {
                    val j = JSONObject(response)
                    val b1 = j.getBoolean(SUCCESS)
                    if (b1) {
                        val sharedPreferences = getSharedPreferences(AUTH, MODE_PRIVATE)
                        val accessToken = sharedPreferences.getString(ACCESS_TOKEN, "")
                        if (accessToken!!.isNotEmpty()) {
                            runOnUiThread {
                                initBiometric()
                            }

                        } else {
                            startActivity(
                                Intent(
                                    this@SplashScreenActivity,
                                    LoginActivity::class.java
                                )
                            )
                            finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(
                                applicationContext,
                                getString(R.string.something_went_wrong_please_try_again),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: JSONException) {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            getString(R.string.something_went_wrong_please_try_again),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("SplashScreenActivity.checkConnection.onResponse.JSONException", e.toString())
                }
            }) { error ->
            Log.e("SplashScreenActivity.checkConnection.onErrorResponse", error.toString())
            runOnUiThread {

                checkConnection()
                if (!isShowing) {
                    isShowing = true
                    noConnection.setTitle(resources.getString(R.string.connection_error_exprition))
                        .setMessage(resources.getString(R.string.connection_error_message))
                        .setPositiveButton(resources.getText(R.string.exit)
                        ) { _, _ -> exitProcess(0) }
                        .setCancelable(false)
                    noConnection.show()
                }

            }
        }

        val q = Volley.newRequestQueue(applicationContext)
        q.add(r)
    }
}