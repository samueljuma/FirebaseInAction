package com.samueljuma.firebaseinaction.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationChannels {

    const val GENERAL_CHANNEL_ID = "general_notifications"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            GENERAL_CHANNEL_ID,
            "General Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Sync status, reminders, and app updates"
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}