package com.samueljuma.firebaseinaction.domain.notifications

interface NotificationDisplayer {
    /**
     * @param deepLink where tapping the notification should navigate
     * (e.g. `"notey://note?noteId=..."`). Null falls back to the generic notifications inbox.
     */
    fun show(
        notificationId: String,
        title: String,
        body: String,
        data: Map<String, String>,
        deepLink: String?
    )
}