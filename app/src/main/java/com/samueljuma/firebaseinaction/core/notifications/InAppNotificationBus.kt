package com.samueljuma.firebaseinaction.core.notifications

import com.samueljuma.core.domain.notifications.AppNotification
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object InAppNotificationBus {

    private val _events = MutableSharedFlow<AppNotification>(
        extraBufferCapacity = Channel.UNLIMITED
    )
    val events: SharedFlow<AppNotification> = _events.asSharedFlow()

    fun emit(notification: AppNotification) {
        _events.tryEmit(notification)
    }
}