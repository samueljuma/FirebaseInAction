package com.samueljuma.firebaseinaction.domain.notifications

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.core.domain.notifications.AppNotification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<AppNotification>>
    fun getUnreadCount(): Flow<Int>
    suspend fun markAsRead(notificationId: String): Result<Unit, DataError>
    suspend fun deleteNotification(notificationId: String): Result<Unit, DataError>
    fun startRemoteSync(): Flow<Unit>
    suspend fun clearLocalData()
}