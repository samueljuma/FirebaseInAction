package com.samueljuma.firebaseinaction.domain.notifications.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val receivedAt: Long = 0L,
    val read: Boolean = false,
    // Where tapping this notification should navigate, e.g. "notey://note?noteId=...".
    // Null falls back to the generic notifications inbox.
    val deepLink: String? = null
)