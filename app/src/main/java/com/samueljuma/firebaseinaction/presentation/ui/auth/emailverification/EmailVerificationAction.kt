package com.samueljuma.firebaseinaction.presentation.ui.auth.emailverification

sealed interface EmailVerificationAction {
    data object OnCheckVerificationClicked : EmailVerificationAction
    data object OnResendEmailClicked : EmailVerificationAction
    data object OnSignOutClicked : EmailVerificationAction
}