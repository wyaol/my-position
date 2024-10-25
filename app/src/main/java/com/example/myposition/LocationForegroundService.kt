package com.example.myposition

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class LocationForegroundService : Service() {

    private lateinit var locationTracker: LocationTracker

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        // 启动前台服务
        startForegroundService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "LocationServiceChannel"
        val manager = getSystemService(NotificationManager::class.java)
        createNotificationChannel(channelId)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_MUTABLE)

        val notification: NotificationCompat.Builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service Running")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText("App is tracking location in the background.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)

        startForeground(1, notification.build())
        Log.d("LocationService", "Started foreground service")

        locationTracker = LocationTracker(this) { location ->
            val updatedNotification = notification.setContentText("location is ${location.latitude} ${location.longitude}.")
            manager.notify(1, updatedNotification.build())
        }
        locationTracker.startLocationUpdates()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String) {
        val channel = NotificationChannel(
            channelId,
            "Location Tracking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
        Log.d("LocationService", "Created notification channel")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // 停止位置更新
    }
}
