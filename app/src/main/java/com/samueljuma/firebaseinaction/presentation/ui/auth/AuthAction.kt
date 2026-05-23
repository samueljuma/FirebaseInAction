package com.samueljuma.firebaseinaction.presentation.ui.auth

sealed interface AuthAction {
    data class OnEmailChanged(val email: String) : AuthAction
    data class OnPasswordChanged(val password: String) : AuthAction
    data object OnTogglePasswordVisibility : AuthAction
    data object OnSignInClicked : AuthAction
    data object OnSignUpClicked : AuthAction
    data object OnGoToSignUp : AuthAction
    data object OnGoToLogin : AuthAction
}