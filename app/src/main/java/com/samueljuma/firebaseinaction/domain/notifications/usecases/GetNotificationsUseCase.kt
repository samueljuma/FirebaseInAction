package com.samueljuma.firebaseinaction.domain.notifications.usecases

import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository
import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification
import kotlinx.coroutines.flow.Flow

class GetNotificationsUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<AppNotification>> = repository.getNotifications()
}