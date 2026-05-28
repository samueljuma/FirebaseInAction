package com.samueljuma.firebaseinaction.presentation.ui.home

import HomeAction
import HomeEvent
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.notes.DeleteNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNotesUseCase
import com.samueljuma.firebaseinaction.domain.notes.StartRemoteSyncUseCase
import com.samueljuma.firebaseinaction.domain.notes.UpdateNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.model.Note
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
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase
) : MviViewModel<HomeState, HomeAction, HomeEvent>(HomeState()) {

    init {
        loadUser()
        observeNotes()
        startSync()
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
                Timber.tag("HomeViewModel").e(e, "Remote sync error")
            }
            .launchIn(viewModelScope)
    }

    override fun onAction(action: HomeAction) {
        when (action) {
            HomeAction.OnSignOutClicked -> signOut()
            is HomeAction.OnDeleteNote -> deleteNote(action.noteId)
            is HomeAction.OnPinNote -> pinNote(action.note)
            HomeAction.OnCreateNoteClicked ->
                emitEvent(HomeEvent.NavigateToCreateNote)
            is HomeAction.OnNoteClicked ->
                emitEvent(HomeEvent.NavigateToNoteDetail(action.noteId))
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess { emitEvent(HomeEvent.NavigateToLogin) }
                .onError { error ->
                    emitEvent(HomeEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun deleteNote(noteId: String) {
        viewModelScope.launch {
            deleteNoteUseCase(noteId)
                .onError { error ->
                    emitEvent(HomeEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun pinNote(note: Note) {
        viewModelScope.launch {
            updateNoteUseCase(note.copy(isPinned = !note.isPinned))
                .onError { error ->
                    emitEvent(HomeEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }
}