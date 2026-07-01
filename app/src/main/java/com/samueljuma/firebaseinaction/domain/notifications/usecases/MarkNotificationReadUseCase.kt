package com.samueljuma.firebaseinaction.domain.notifications.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository

class MarkNotificationReadUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit, DataError> =
        repository.markAsRead(notificationId)
}