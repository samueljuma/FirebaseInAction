package com.samueljuma.firebaseinaction.presentation.ui.auth.signin

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)
