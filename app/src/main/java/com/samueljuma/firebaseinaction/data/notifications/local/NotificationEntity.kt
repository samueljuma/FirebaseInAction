package com.samueljuma.firebaseinaction.data.notifications.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val body: String,
    val receivedAt: Long,
    val read: Boolean = false
)