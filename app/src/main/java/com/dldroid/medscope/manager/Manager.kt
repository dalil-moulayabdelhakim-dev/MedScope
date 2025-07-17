package com.dldroid.medscope.manager

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dldroid.medscope.R

class Manager(private val context: Context) {

    private fun checkPermission(context: Context, permission: String): Boolean {
        val check = ContextCompat.checkSelfPermission(context, permission)
        return (check == PackageManager.PERMISSION_GRANTED)
    }

    fun uploadFile(url: String) {
        Log.d("URL", url)
        val uri = Uri.parse(url)

        val downloadmanager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        if (checkPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (checkPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                val request = DownloadManager.Request(uri)
                request.setTitle(context.getString(R.string.app_name))
                request.setDescription("Downloading...")
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setVisibleInDownloadsUi(false)
                val fileName =
                    url.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                Log.d("FILE", fileName[fileName.size - 1])
                request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    fileName[fileName.size - 1]
                )
                downloadmanager.enqueue(request)
            } else {
                val READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!,
                    arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1
            ActivityCompat.requestPermissions(
                (context as Activity?)!!,
                arrayOf<String>(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun sendSMS(num: String?, msg: String?) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(num, null, msg, null, null)
    }


}