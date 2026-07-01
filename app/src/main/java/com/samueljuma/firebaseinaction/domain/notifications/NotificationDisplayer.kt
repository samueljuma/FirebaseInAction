package com.samueljuma.firebaseinaction.domain.notifications

interface NotificationDisplayer {
    fun show(notificationId: String, title: String, body: String, data: Map<String, String>)
}