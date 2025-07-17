package com.dldroid.medscope.manager

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.dldroid.medscope.R
import com.dldroid.medscope.database.PDF_CHANNEL_ID
import com.dldroid.medscope.manager.services.DownloadService
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import kotlin.math.min

class FileDownloader(private var activity: Activity, private val launcher: ActivityResultLauncher<Intent>) : AsyncTask<String, Int, String>() {

    private var mNotifyManager: NotificationManager? = null
    private var build: NotificationCompat.Builder? = null

    private var fileURL: String? = null


    override fun onPreExecute() {
        super.onPreExecute()
       /* val downloadDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            activity.getString(R.string.app_name)
        )

        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }
*/
        mNotifyManager =
            activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        build = NotificationCompat.Builder(activity)
        val cancelIntent = Intent(activity, DownloadService::class.java)
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val cancelPendingIntent =
            PendingIntent.getActivity(activity, 0, cancelIntent, PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            build!!.setChannelId("$PDF_CHANNEL_ID ")
                .addAction(0, activity.getString(R.string.cancel), cancelPendingIntent)
                .setAutoCancel(false)
                .setDefaults(0)
                .setOngoing(true)
                .setSmallIcon(R.drawable.logo_svg)
        }

        // Since android 8.0 notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "$PDF_CHANNEL_ID ",
                "PDF Manager",
                NotificationManager.IMPORTANCE_HIGH
            )

            channel.description = "Notification will show just when pdf file downloading."
            channel.setSound(null, null)
            channel.enableLights(true)
            channel.lightColor = activity.resources.getColor(R.color.blue)
            channel.enableVibration(false)
            mNotifyManager!!.createNotificationChannel(channel)
        }
        build!!.setProgress(100, 0, false)
        mNotifyManager!!.notify(PDF_CHANNEL_ID, build!!.build())
    }

    override fun doInBackground(vararg mUrl: String): String? {
        var count: Int
        try {
            val url = URL(mUrl[0])
            val conection = url.openConnection()
            conection.connect()
            val lenghtOfFile = conection.contentLength

            val input: InputStream = BufferedInputStream(
                url.openStream(),
                8192
            )

            // Output stream
            val fileArr = mUrl[0].split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val fileName = fileArr[fileArr.size - 1]
            build!!.setContentTitle(fileName)
            fileURL = File(activity.filesDir, fileName).absolutePath
            Log.d("PATH", fileURL!!)
            val output: OutputStream = FileOutputStream(fileURL)
            val data = ByteArray(1024)

            var total: Long = 0

            while ((input.read(data).also { count = it }) != -1) {
                total += count.toLong()
                val cur = ((total * 100) / lenghtOfFile).toInt()

                publishProgress(min(cur, 100))
                if (min(cur.toDouble(), 100.0) > 98) {
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        Log.d("Failure", "sleeping failure")
                    }
                }
                Log.i(
                    "currentProgress",
                    """currentProgress: ${min(cur.toDouble(), 100.0)}
 $cur"""
                )

                output.write(data, 0, count)
            }

            output.flush()

            output.close()
            input.close()
        } catch (e: Exception) {
            Log.e("MYER", e.message!!)
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        build!!.setProgress(100, values[0]!!, false)
            .setContentText(values[0].toString() + "%")
        mNotifyManager!!.notify(PDF_CHANNEL_ID, build!!.build())

    }


    override fun onPostExecute(fileUrl: String?) {
        build!!.setContentTitle("Download complete")
            .setProgress(0, 0, false)
            .setOngoing(false)
        mNotifyManager!!.notify(PDF_CHANNEL_ID, build!!.build())
        fileURL?.let { File(it) }?.let { openPdf(activity, it) }
    }

    /*
    private fun openPDF() {
        try {
            val file = fileURL?.let { File(it) }
            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = getMimeType(fileURL)
            intent.setDataAndType(Uri.fromFile(file), mimeType)
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("MY_ERROR_AT_OPEN_PDF", e.toString())
        }
    }
     */
    fun openPdf(activity: Activity, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                activity,
                "${activity.packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW)
            val mimeType = getMimeType(file.absolutePath)
            intent.setDataAndType(uri, mimeType)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

            launcher.launch(intent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, "Cannot open file", Toast.LENGTH_SHORT).show()
        }

    }



    private fun getMimeType(path: String?): String? {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path) ?: return "application/pdf"
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/pdf"
    }
}