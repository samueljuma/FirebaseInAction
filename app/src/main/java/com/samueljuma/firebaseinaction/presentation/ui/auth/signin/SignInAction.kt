package com.samueljuma.firebaseinaction.presentation.ui.auth.signin

sealed interface SignInAction {
    data class OnEmailChanged(val email: String) : SignInAction
    data class OnPasswordChanged(val password: String) : SignInAction
    data object OnTogglePasswordVisibility : SignInAction
    data object OnSignInClicked : SignInAction
    data object OnGoogleSignInClicked : SignInAction
    data object OnGoToSignUp : SignInAction
}
