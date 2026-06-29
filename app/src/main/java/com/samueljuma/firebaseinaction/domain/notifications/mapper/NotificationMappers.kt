package com.samueljuma.firebaseinaction.domain.notifications.mapper

import com.samueljuma.firebaseinaction.data.notifications.local.NotificationEntity
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification

fun NotificationEntity.toDomain() = AppNotification(
    id = id,
    title = title,
    body = body,
    receivedAt = receivedAt,
    read = read
)

fun AppNotification.toEntity() = NotificationEntity(
    id = id,
    title = title,
    body = body,
    receivedAt = receivedAt,
    read = read
)