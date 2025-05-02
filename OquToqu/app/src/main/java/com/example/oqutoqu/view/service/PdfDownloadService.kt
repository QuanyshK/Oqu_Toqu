package com.example.oqutoqu.view.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.example.data.utils.FileDownloader
import com.example.oqutoqu.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class PdfDownloadService : Service() {
    private val channelId = "pdf_download_channel"
    private val notificationId = 1001
    private val TAG = "PdfDownloadService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")

        createNotificationChannel()

        val pdfUrl = intent?.getStringExtra("pdf_url")
        if (pdfUrl.isNullOrBlank()) {
            Log.e(TAG, "No PDF URL found in intent")
            stopSelf()
            return START_NOT_STICKY
        }

        Log.d(TAG, "Downloading from URL: $pdfUrl")
        startForeground(notificationId, createProgressNotification())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = FileDownloader.downloadPdfToDownloads(this@PdfDownloadService, pdfUrl)
                if (file != null) {
                    Log.d(TAG, "Download successful: ${file.absolutePath}")
                    showDownloadCompleteNotification(file)
                    sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).apply {
                        data = Uri.fromFile(file)
                    })
                } else {
                    Log.e(TAG, "Download returned null file")
                    showDownloadFailed()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while downloading: ${e.message}")
                showDownloadFailed()
            }
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val channel = NotificationChannel(
                    channelId,
                    "File Downloads",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Shows download progress and completion status"
                }
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
                Log.d(TAG, "Notification channel created")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create notification channel: ${e.message}")
            }
        }
    }

    private fun createProgressNotification(): Notification {
        Log.d(TAG, "Creating progress notification")
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Downloading PDF")
            .setContentText("File is being downloaded...")
            .setSmallIcon(R.drawable.scihub)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun showDownloadCompleteNotification(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val openIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Download Complete")
                .setContentText("Tap to open ${file.name}")
                .setSmallIcon(R.drawable.scihub)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId + 1, notification)
            Log.d(TAG, "Download complete notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show complete notification: ${e.message}")
        }
    }

    private fun showDownloadFailed() {
        try {
            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Download Failed")
                .setContentText("Could not download file")
                .setSmallIcon(R.drawable.scihub)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(notificationId + 2, notification)
            Log.d(TAG, "Download failed notification shown")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show failure notification: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
