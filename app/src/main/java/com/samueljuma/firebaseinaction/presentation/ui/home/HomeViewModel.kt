package com.samueljuma.firebaseinaction.presentation.ui.home

import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel

class HomeViewModel(
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase
): MviViewModel<HomeState, HomeAction, HomeEvent>(
    initialState = HomeState()
) {

    init {
         updateState { copy( loggedInUser = getCurrentUserSyncUseCase()) }
    }
    override fun onAction(action: HomeAction) {
        TODO("Not yet implemented")
    }
}