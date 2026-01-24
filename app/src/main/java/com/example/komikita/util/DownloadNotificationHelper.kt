package com.example.komikita.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.komikita.R

object DownloadNotificationHelper {
    
    private const val CHANNEL_ID = "download_channel"
    private const val CHANNEL_NAME = "Downloads"
    private const val NOTIFICATION_ID = 1001
    
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Download progress notifications"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun showDownloadProgressNotification(
        context: Context, 
        totalChapters: Int,
        currentChapter: Int,
        chapterName: String
    ) {
        createNotificationChannel(context)
        
        val progress = ((currentChapter.toFloat() / totalChapters) * 100).toInt()
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download)
            .setContentTitle("Downloading Komik")
            .setContentText("Chapter $chapterName ($currentChapter/$totalChapters)")
            .setProgress(100, progress, false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted, skip notification
        }
    }
    
    fun showDownloadCompleteNotification(context: Context, totalChapters: Int) {
        createNotificationChannel(context)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_download_done)
            .setContentTitle("Download Selesai")
            .setContentText("$totalChapters chapter berhasil di-download")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted, skip notification
        }
    }
    
    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
