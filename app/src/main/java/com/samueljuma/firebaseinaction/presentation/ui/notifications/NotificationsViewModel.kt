package com.samueljuma.firebaseinaction.presentation.ui.notifications

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.domain.notifications.usecases.DeleteNotificationUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.GetNotificationsUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.MarkNotificationReadUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class NotificationsViewModel(
    savedStateHandle: SavedStateHandle,
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase,
    private val deleteNotificationUseCase: DeleteNotificationUseCase,
    // Application-lifetime scope (DI). Used so pending "read" writes survive this
    // ViewModel being cleared on back-navigation.
    private val applicationScope: CoroutineScope
) : MviViewModel<NotificationsState, NotificationsAction, NotificationsEvent>(NotificationsState()) {

    init {
        // When opened from the in-app banner, the tapped notification id is passed
        // along so it is flushed to "read" when the user leaves the screen.
        val openedFromId: String? = savedStateHandle["notificationId"]
        if (openedFromId != null) {
            updateState { copy(pendingReadIds = pendingReadIds + openedFromId) }
        }
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
                // Defer the read write so the card doesn't reshuffle/disappear while
                // the user is still on the screen — flushed in flushPendingReads().
                updateState { copy(pendingReadIds = pendingReadIds + action.notificationId) }
            is NotificationsAction.OnDeleteNotification ->
                deleteNotification(action.notificationId)
            is NotificationsAction.OnTabSelected ->
                updateState { copy(selectedTab = action.tab) }
        }
    }

    private fun deleteNotification(id: String) {
        viewModelScope.launch {
            deleteNotificationUseCase(id)
        }
    }

    /**
     * Commits all notifications the user tapped to "read" in Room + Firestore.
     * Runs on the application scope so the writes complete even as this ViewModel
     * is being cleared on back-navigation.
     */
    fun flushPendingReads() {
        val ids = state.value.pendingReadIds
        if (ids.isEmpty()) return
        applicationScope.launch {
            ids.map { id -> async { markNotificationReadUseCase(id) } }.awaitAll()
        }
    }
}
