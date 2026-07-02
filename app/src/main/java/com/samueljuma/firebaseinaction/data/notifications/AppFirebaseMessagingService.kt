package com.samueljuma.firebaseinaction.data.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samueljuma.firebaseinaction.core.lifecycle.AppForegroundTracker
import com.samueljuma.firebaseinaction.core.notifications.InAppNotificationBus
import com.samueljuma.firebaseinaction.domain.notifications.NotificationDisplayer
import com.samueljuma.firebaseinaction.domain.notifications.PushTokenRepository
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import com.samueljuma.firebaseinaction.domain.notifications.model.FcmPayloadKind
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.UUID

class AppFirebaseMessagingService : FirebaseMessagingService() {

    private val pushTokenRepository: PushTokenRepository by inject()
    private val notificationDisplayer: NotificationDisplayer by inject()

    override fun onNewToken(token: String) {
        Timber.tag("FCM").d("Token refreshed")
        CoroutineScope(Dispatchers.IO).launch {
            pushTokenRepository.saveTokenForCurrentUser(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        Timber.tag("FCM").d("Message received: $data")

        // A DISPLAY message (has a `notification` block, e.g. a Console campaign) never reaches
        // this method at all while the app is backgrounded — Android auto-displays it via the
        // system tray and skips onMessageReceived entirely. So this branch only ever matters for
        // the foreground path below; the background path further down is DATA-only by construction.
        val kind = if (message.notification != null) FcmPayloadKind.DISPLAY else FcmPayloadKind.DATA

        // The sending Cloud Function authors the Firestore inbox doc itself and stamps its id
        // into the data payload — using that id (not message.messageId, a transient FCM id)
        // keeps this client-side notification and the eventual Firestore-synced row the same
        // entity. The client only *displays*; it never writes to Firestore. Fallback to a random
        // id covers messages sent without notificationId (e.g. manual console testing).
        val notification = AppNotification(
            id = data["notificationId"] ?: message.messageId ?: UUID.randomUUID().toString(),
            // DISPLAY messages carry their real content in message.notification, not data —
            // prefer that so a Console campaign shows its actual text, not our generic fallback.
            title = message.notification?.title ?: data["title"] ?: "Notey",
            body = message.notification?.body
                ?: data["body"]
                ?: "This is a sample body of the message you would receive for any notifications sent to you",
            receivedAt = System.currentTimeMillis(),
            read = false,
            deepLink = data["deepLink"],
            kind = kind
        )

        if (kind == FcmPayloadKind.DISPLAY) {
            Timber.tag("FCM").i(
                "Display notification received: id=${notification.id} foreground=${AppForegroundTracker.isAppInForeground}"
            )
        }

        if (AppForegroundTracker.isAppInForeground) {
            InAppNotificationBus.emit(notification)
        } else {
            notificationDisplayer.show(
                notificationId = notification.id,
                title = notification.title,
                body = notification.body,
                data = data,
                deepLink = notification.deepLink
            )
        }
    }
}