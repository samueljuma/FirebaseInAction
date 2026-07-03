package com.samueljuma.firebaseinaction.presentation.ui.home

import HomeAction
import HomeEvent
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.utils.UiText
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.notes.DeleteNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNotesUseCase
import com.samueljuma.firebaseinaction.domain.notes.StartRemoteSyncUseCase
import com.samueljuma.firebaseinaction.domain.notes.UpdateNoteUseCase
import com.samueljuma.firebaseinaction.domain.config.FeatureFlags
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import com.samueljuma.firebaseinaction.domain.notifications.usecases.GetUnreadCountUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.StartNotificationSyncUseCase
import com.samueljuma.firebaseinaction.domain.sync.SyncScheduler
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val startRemoteSyncUseCase: StartRemoteSyncUseCase,
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase,
    private val syncScheduler: SyncScheduler,
    private val getUnreadCountUseCase: GetUnreadCountUseCase,
    private val startNotificationSyncUseCase: StartNotificationSyncUseCase,
    private val featureFlags: FeatureFlags
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {

    init {
        loadUser()
        observeNotes()
        startSync()
        observeUnreadCount()
        startNotificationSync()
        observeWelcomeMessage()
    }

    private fun loadUser() {
        updateState { copy(loggedInUser = getCurrentUserSyncUseCase()) }
    }

    private fun observeNotes() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            getNotesUseCase()
                .collect { notes ->
                    updateState {
                        copy(
                            notes = notes,
                            isLoading = false
                        )
                    }
                }
        }
    }

    private fun startSync() {
        // Fire and forget — side effect only
        // Lifecycle tied to viewModelScope
        // Cancelled automatically when ViewModel is cleared on sign out
        startRemoteSyncUseCase()
            .onEach { }
            .catch { e ->
                Timber.tag(TAG).e(e, "Remote sync error")
            }
            .launchIn(viewModelScope)
    }

    private fun observeUnreadCount() {
        viewModelScope.launch {
            getUnreadCountUseCase().collect { count ->
                updateState { copy(unreadCount = count) }
            }
        }
    }

    private fun startNotificationSync() {
        startNotificationSyncUseCase()
            .onEach { }
            .catch { e -> Timber.tag(TAG).e(e, "Notification sync error") }
            .launchIn(viewModelScope)
    }

    private fun observeWelcomeMessage() {
        featureFlags.config
            .onEach { config -> updateState { copy(welcomeMessage = config.welcomeMessage) } }
            .launchIn(viewModelScope)
    }

    private fun onCreateNoteClicked() {
        // Free-tier gate: block creation once the remotely-configured limit is reached.
        val limit = featureFlags.config.value.maxFreeNotes
        if (state.value.notes.size >= limit) {
            emitEvent(
                HomeEvent.ShowSnackbar(UiText.StringResource(R.string.note_limit_reached, limit))
            )
            return
        }
        emitEvent(HomeEvent.NavigateToCreateNote)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnSignOutClicked -> signOut()
            is HomeAction.OnDeleteNote -> deleteNote(action.note)
            is HomeAction.OnPinNote -> pinNote(action.note)
            HomeAction.OnCreateNoteClicked -> onCreateNoteClicked()
            is HomeAction.OnNoteClicked ->
                emitEvent(HomeEvent.NavigateToNoteDetail(action.noteId))
            HomeAction.OnNotificationsClicked ->
                emitEvent(HomeEvent.NavigateToNotifications)
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess {
                    syncScheduler.cancelAllSyncs()
                    emitEvent(HomeEvent.NavigateToLogin)
                }
                .onError { error ->
                    emitEvent(HomeEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun deleteNote(note: Note) {
        viewModelScope.launch {
            deleteNoteUseCase(
                noteId = note.id,
                noteCreatedAt = note.createdAt,
                hadImage = note.imageUrl !=null,
            )
                .onError { error ->
                    emitEvent(HomeEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun pinNote(note: Note) {
        viewModelScope.launch {
            updateNoteUseCase(note.copy(pinned = !note.pinned))
                .onError { error ->
                    emitEvent(HomeEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
    }
}
