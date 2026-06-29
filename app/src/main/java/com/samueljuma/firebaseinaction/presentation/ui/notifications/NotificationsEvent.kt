package com.samueljuma.firebaseinaction.presentation.ui.notifications

sealed interface NotificationsEvent {
    data object NavigateBack : NotificationsEvent
}