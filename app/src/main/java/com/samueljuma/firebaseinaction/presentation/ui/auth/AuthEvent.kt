package com.samueljuma.firebaseinaction.presentation.ui.auth

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface AuthEvent {
    // Login screen events
    sealed interface Login : AuthEvent {
        data object NavigateToHome : Login
        data class ShowSnackbar(val message: UiText) : Login
    }

    // SignUp screen events
    sealed interface SignUp : AuthEvent {
        data object NavigateToHome : SignUp
        data class ShowSnackbar(val message: UiText) : SignUp
    }
}