package com.samueljuma.firebaseinaction.data.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samueljuma.firebaseinaction.core.lifecycle.AppForegroundTracker
import com.samueljuma.firebaseinaction.core.notifications.InAppNotificationBus
import com.samueljuma.firebaseinaction.domain.notifications.NotificationDisplayer
import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository
import com.samueljuma.firebaseinaction.domain.notifications.PushTokenRepository
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val pushTokenRepository: PushTokenRepository by inject()
    private val notificationDisplayer: NotificationDisplayer by inject()
    private val notificationRepository: NotificationRepository by inject()

    override fun onNewToken(token: String) {
        Timber.tag("FCM").d("Token refreshed")
        CoroutineScope(Dispatchers.IO).launch {
            pushTokenRepository.saveTokenForCurrentUser(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        Timber.tag("FCM").d("Message received: $data")

        val notification = AppNotification(
            id = message.messageId ?: UUID.randomUUID().toString(),
            title = data["title"] ?: "FirebaseInAction",
            body = data["body"] ?: "",
            receivedAt = System.currentTimeMillis(),
            read = false
        )

        // Persist regardless of foreground/background — the inbox is always the source of truth.
        CoroutineScope(Dispatchers.IO).launch {
            notificationRepository.saveNotification(notification)
        }

        if (AppForegroundTracker.isAppInForeground) {
            InAppNotificationBus.emit(notification)
        } else {
            notificationDisplayer.show(
                title = notification.title,
                body = notification.body,
                data = data
            )
        }
    }
}