package com.samueljuma.firebaseinaction.data.notifications.remote

import com.samueljuma.firebaseinaction.data.notifications.local.NotificationEntity
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification

/**
 * Firestore wire model for a notification. Keeps the Firestore document schema
 * decoupled from the domain model ([AppNotification]) and the Room entity.
 * Defaults are required for Firestore's reflective deserialization.
 */
data class NotificationDto(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val receivedAt: Long = 0L,
    val read: Boolean = false
)

fun AppNotification.toDto() = NotificationDto(
    id = id,
    title = title,
    body = body,
    receivedAt = receivedAt,
    read = read
)

fun NotificationDto.toEntity() = NotificationEntity(
    id = id,
    title = title,
    body = body,
    receivedAt = receivedAt,
    read = read
)
