package com.samueljuma.firebaseinaction.presentation.ui.notifications

import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification

data class NotificationsState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val selectedTab: NotificationTab = NotificationTab.ALL,
    val pendingReadIds: Set<String> = emptySet()
) {
    /** Notifications to display for [tab]. Presentation derivation, not UI logic. */
    fun notificationsFor(tab: NotificationTab): List<AppNotification> =
        tab.filter(notifications)
}
