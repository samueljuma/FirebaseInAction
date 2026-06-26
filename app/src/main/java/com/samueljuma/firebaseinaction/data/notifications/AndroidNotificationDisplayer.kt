package com.samueljuma.firebaseinaction.data.notifications

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.notifications.NotificationChannels
import com.samueljuma.firebaseinaction.domain.notifications.NotificationDisplayer
import timber.log.Timber

class AndroidNotificationDisplayer(
    private val context: Context
) : NotificationDisplayer {

    override fun show(title: String, body: String, data: Map<String, String>) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasPermission) {
            Timber.tag("FCM").w("Notification permission not granted — skipping display")
            return
        }

        val notification = NotificationCompat.Builder(
            context, NotificationChannels.GENERAL_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notey_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification)
    }
}