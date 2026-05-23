package com.samueljuma.firebaseinaction.presentation.ui.home

sealed interface HomeAction {
    data object OnLogoutClick: HomeAction
}