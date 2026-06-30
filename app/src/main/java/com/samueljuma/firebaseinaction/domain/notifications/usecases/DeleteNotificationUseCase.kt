package com.samueljuma.firebaseinaction.domain.notifications.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository

class DeleteNotificationUseCase(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String): Result<Unit, DataError> =
        repository.deleteNotification(notificationId)
}
