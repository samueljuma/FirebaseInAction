package com.samueljuma.firebaseinaction.presentation.ui.notifications

import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification

data class NotificationsState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true
)