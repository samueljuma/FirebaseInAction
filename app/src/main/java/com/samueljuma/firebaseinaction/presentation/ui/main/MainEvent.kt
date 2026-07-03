package com.samueljuma.firebaseinaction.presentation.ui.main

sealed interface MainEvent {
    data object SessionExpired : MainEvent
}