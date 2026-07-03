package com.samueljuma.firebaseinaction.core.notifications

/**
 * Single source of truth for the app's notification deep link.
 *
 * [PATTERN] (with the `{notificationId}` placeholder) is registered on the
 * Notifications destination via `navDeepLink`, and must stay in sync with the
 * `<intent-filter>` for MainActivity in AndroidManifest.xml.
 *
 * [uri] builds the concrete link that the system notification's PendingIntent
 * fires when tapped.
 */
object NotificationDeepLinks {
    const val SCHEME = "notey"
    const val HOST = "notification"

    const val PATTERN = "$SCHEME://$HOST?notificationId={notificationId}"

    fun uri(notificationId: String) = "$SCHEME://$HOST?notificationId=$notificationId"
}
