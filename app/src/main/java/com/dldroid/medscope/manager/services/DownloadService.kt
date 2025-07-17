package com.dldroid.medscope.manager.services

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.dldroid.medscope.database.PDF_CHANNEL_ID

class DownloadService: Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Add your cancellation logic here
        cancelDownload()

        // Stop the service
        stopSelf()

        return super.onStartCommand(intent, flags, startId)
    }

    private fun cancelDownload() {
        // Implement your cancellation logic here
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(PDF_CHANNEL_ID)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}