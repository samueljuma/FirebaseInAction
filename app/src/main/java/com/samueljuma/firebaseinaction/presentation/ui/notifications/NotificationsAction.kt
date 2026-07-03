package com.samueljuma.firebaseinaction.presentation.ui.notifications

sealed interface NotificationsAction {
    data object OnBackClicked : NotificationsAction
    data class OnNotificationClicked(val notificationId: String) : NotificationsAction
    data class OnDeleteNotification(val notificationId: String) : NotificationsAction
    data class OnTabSelected(val tab: NotificationTab) : NotificationsAction
}
