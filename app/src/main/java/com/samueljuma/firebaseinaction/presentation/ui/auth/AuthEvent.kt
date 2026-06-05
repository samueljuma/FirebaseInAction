package com.samueljuma.firebaseinaction.presentation.ui.auth

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface AuthEvent {
    // Login screen events
    sealed interface Login : AuthEvent {
        data object LoginSuccess : Login
        data class ShowSnackbar(val message: UiText) : Login
        data object OpenGoogleAccountSettings : Login
    }

    // SignUp screen events
    sealed interface SignUp : AuthEvent {
        data object SignUpSuccess : SignUp
        data class ShowSnackbar(val message: UiText) : SignUp
        data object OpenGoogleAccountSettings : SignUp
    }
}