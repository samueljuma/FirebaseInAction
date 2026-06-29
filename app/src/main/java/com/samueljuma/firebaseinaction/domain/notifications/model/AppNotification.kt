package com.samueljuma.firebaseinaction.domain.notifications.model

data class AppNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val receivedAt: Long = 0L,
    val read: Boolean = false
)