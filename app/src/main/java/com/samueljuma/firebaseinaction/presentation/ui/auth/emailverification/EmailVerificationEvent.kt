package com.samueljuma.firebaseinaction.presentation.ui.auth.emailverification

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface EmailVerificationEvent {
    data object NavigateToHome : EmailVerificationEvent
    data object NavigateToLogin : EmailVerificationEvent
    data class ShowSnackbar(val message: UiText) : EmailVerificationEvent
}