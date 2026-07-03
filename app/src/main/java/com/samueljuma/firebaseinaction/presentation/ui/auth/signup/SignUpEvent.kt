package com.samueljuma.firebaseinaction.presentation.ui.auth.signup

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface SignUpEvent {
    data object SignUpSuccess : SignUpEvent
    data class ShowSnackbar(val message: UiText) : SignUpEvent
    data object OpenGoogleAccountSettings : SignUpEvent
}