package com.samueljuma.firebaseinaction.domain.notifications.usecases

import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository
import kotlinx.coroutines.flow.Flow

class GetUnreadCountUseCase(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<Int> = repository.getUnreadCount()
}