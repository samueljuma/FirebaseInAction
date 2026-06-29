package com.samueljuma.firebaseinaction.presentation.ui.notifications

import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.domain.notifications.usecases.GetNotificationsUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.MarkNotificationReadUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase
) : MviViewModel<NotificationsState, NotificationsAction, NotificationsEvent>(NotificationsState()) {

    init {
        observeNotifications()
    }

    private fun observeNotifications() {
        viewModelScope.launch {
            getNotificationsUseCase().collect { list ->
                updateState { copy(notifications = list, isLoading = false) }
            }
        }
    }

    override fun onAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.OnBackClicked ->
                emitEvent(NotificationsEvent.NavigateBack)
            is NotificationsAction.OnNotificationClicked ->
                markRead(action.notificationId)
        }
    }

    private fun markRead(id: String) {
        viewModelScope.launch {
            markNotificationReadUseCase(id)
        }
    }
}