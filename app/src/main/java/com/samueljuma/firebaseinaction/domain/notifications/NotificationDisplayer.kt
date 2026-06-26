package com.samueljuma.firebaseinaction.domain.notifications

interface NotificationDisplayer {
    fun show(title: String, body: String, data: Map<String, String>)
}