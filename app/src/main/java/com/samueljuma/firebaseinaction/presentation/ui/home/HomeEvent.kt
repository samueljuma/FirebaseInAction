package com.samueljuma.firebaseinaction.presentation.ui.home

sealed interface HomeEvent {
    data object OnSessionExpired: HomeEvent
}