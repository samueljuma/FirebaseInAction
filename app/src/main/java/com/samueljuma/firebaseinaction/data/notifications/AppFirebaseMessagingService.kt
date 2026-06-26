package com.samueljuma.firebaseinaction.data.notifications

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samueljuma.firebaseinaction.core.lifecycle.AppForegroundTracker
import com.samueljuma.firebaseinaction.domain.notifications.NotificationDisplayer
import com.samueljuma.firebaseinaction.domain.notifications.PushTokenRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

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
        Timber.tag("FCM").d("Message received: ${message.notification}")

        if (AppForegroundTracker.isAppInForeground) {
            // App is open — don't pollute the system tray with a redundant alert.
            // A different mechanism (e.g. a SharedFlow the UI collects) could
            // surface an in-app banner/snackbar here instead. We'll wire that
            // when we have a real notification type to react to.
            Timber.tag("FCM").d("App in foreground — suppressing system notification")
        } else {
            notificationDisplayer.show(
                title = data["title"] ?: "FirebaseInAction",
                body = data["body"] ?: "",
                data = data
            )
        }
    }
}