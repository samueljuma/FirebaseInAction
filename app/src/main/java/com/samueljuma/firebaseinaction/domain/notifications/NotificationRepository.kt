package com.samueljuma.firebaseinaction.domain.notifications

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun saveNotification(notification: AppNotification): Result<Unit, DataError>
    suspend fun markAsRead(notificationId: String): Result<Unit, DataError>
    fun startRemoteSync(): Flow<Unit>
    suspend fun clearLocalData()
}