package com.example.kotlinconversionsupportivehousing.helpers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context


object NotificationHelper {
    fun createNotificationChannel(
        context: Context,
        notificationID: String?,
        notificationName: String?
    ) {
        val channel = NotificationChannel(
            notificationID,
            notificationName,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = context.getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
    }
}