package com.dldroid.medscope.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dldroid.medscope.database.DEVICE_MODEL
import com.dldroid.medscope.database.PERMISSION_REQUEST

class PermissionsManager {

    companion object {
        fun cameraPermission(context: Context?): Boolean {
            val activity = context as Activity

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST
                )
                return false
            }
            return true
        }



        fun readExternalStoragePermission(context: Context?): Boolean {
            val activity = context as Activity

            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST
                )
                return false
            }
            return true
        }

        fun writeExternalStoragePermission(context: Context?): Boolean {
            val activity = context as Activity


            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST
                )
                return false
            }
            return true
        }

        fun sendSMSPermission(context: Context?): Boolean {
            val activity = context as Activity



            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS,
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.SEND_SMS),
                    PERMISSION_REQUEST
                )
                return false
            }
            return true
        }

        fun postNotificationPermission(context: Context?):Boolean{
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU){
                val activity = context as Activity
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST
                    )
                    return false
                }
            }
            return true
        }

        fun foregroundSpecialUsePermission(context: Context?): Boolean {
            val activity = context as Activity


            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE),
                    PERMISSION_REQUEST
                )
                return false
            }
            return true
        }

        fun bluetoothConnectPermission(context: Context?): Boolean{
            val activity = context as Activity


            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ), PERMISSION_REQUEST
                    )
                }
                return false
            }
            return true
        }

        fun checkPermission(context: Context?, permission: String?):Boolean{
            return ContextCompat.checkSelfPermission(
                context!!,
                permission!!
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}