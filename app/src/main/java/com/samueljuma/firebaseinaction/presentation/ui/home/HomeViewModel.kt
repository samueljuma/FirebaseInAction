package com.samueljuma.firebaseinaction.presentation.ui.home

import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel

class HomeViewModel: MviViewModel<HomeState, HomeAction, HomeEvent>(
    initialState = HomeState()
) {
    override fun onAction(action: HomeAction) {
        TODO("Not yet implemented")
    }
}