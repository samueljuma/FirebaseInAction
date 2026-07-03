package com.samueljuma.firebaseinaction.presentation.ui.auth.signup

sealed interface SignUpAction {
    data class OnEmailChanged(val email: String) : SignUpAction
    data class OnPasswordChanged(val password: String) : SignUpAction
    data object OnTogglePasswordVisibility : SignUpAction
    data object OnSignUpClicked : SignUpAction
    data object OnGoogleSignInClicked : SignUpAction
    data object OnGoToLogin : SignUpAction
}
