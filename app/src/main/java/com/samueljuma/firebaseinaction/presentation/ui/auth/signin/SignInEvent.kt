package com.samueljuma.firebaseinaction.presentation.ui.auth.signin

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface SignInEvent {
    data object SignInSuccess : SignInEvent
    data class ShowSnackbar(val message: UiText) : SignInEvent
    data object OpenGoogleAccountSettings : SignInEvent
}
