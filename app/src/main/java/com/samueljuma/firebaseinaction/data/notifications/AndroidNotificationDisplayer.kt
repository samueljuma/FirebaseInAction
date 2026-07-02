package com.samueljuma.firebaseinaction.data.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.notifications.NotificationChannels
import com.samueljuma.firebaseinaction.core.notifications.NotificationDeepLinks
import com.samueljuma.firebaseinaction.domain.notifications.NotificationDisplayer
import com.samueljuma.firebaseinaction.presentation.ui.main.MainActivity
import timber.log.Timber

class AndroidNotificationDisplayer(
    private val context: Context
) : NotificationDisplayer {

    override fun show(
        notificationId: String,
        title: String,
        body: String,
        data: Map<String, String>,
        deepLink: String?
    ) {
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
            .setContentIntent(deepLinkPendingIntent(notificationId, deepLink))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context)
            .notify(notificationId.hashCode(), notification)
    }

    /**
     * PendingIntent that fires the in-app deep link when the system notification
     * is tapped. Defaults to the notifications inbox, but honors a specific
     * [deepLink] (e.g. a reminder's note) when the sender supplied one.
     * TaskStackBuilder synthesizes a proper back stack (Home -> destination) so
     * Back returns to the app's home, and works from a cold start — the
     * Navigation graph parses the URI and hands the id to the destination's
     * SavedStateHandle.
     */
    private fun deepLinkPendingIntent(notificationId: String, deepLink: String?): PendingIntent {
        val uri = (deepLink ?: NotificationDeepLinks.uri(notificationId)).toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(
                notificationId.hashCode(),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )!!
        }
    }
}