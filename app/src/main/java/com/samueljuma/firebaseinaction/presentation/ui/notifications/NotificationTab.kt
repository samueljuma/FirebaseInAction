package com.samueljuma.firebaseinaction.presentation.ui.notifications

import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification

/**
 * The tabs shown on the notifications screen. Owning the title, empty-state copy
 * and the filtering rule here keeps that logic out of the composable.
 */
enum class NotificationTab(val title: String, val emptyMessage: String) {
    ALL("All", "No notifications yet") {
        override fun filter(notifications: List<AppNotification>) = notifications
    },
    UNREAD("Unread", "No unread notifications") {
        override fun filter(notifications: List<AppNotification>) =
            notifications.filter { !it.read }
    };

    abstract fun filter(notifications: List<AppNotification>): List<AppNotification>
}
