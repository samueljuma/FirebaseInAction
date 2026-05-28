package com.samueljuma.firebaseinaction.presentation.ui.home

import HomeAction
import HomeEvent
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.notes.DeleteNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNotesUseCase
import com.samueljuma.firebaseinaction.domain.notes.StartRemoteSyncUseCase
import com.samueljuma.firebaseinaction.domain.notes.UpdateNoteUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel

class HomeViewModel(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val startRemoteSyncUseCase: StartRemoteSyncUseCase,
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase
): MviViewModel<HomeState, HomeAction, HomeEvent>(
    initialState = HomeState()
) {

    init {
         loadUser()
    }

    private fun loadUser() {
        updateState { copy(loggedInUser = getCurrentUserSyncUseCase()) }
    }
    override fun onAction(action: HomeAction) {
        TODO("Not yet implemented")
    }
}