package com.samueljuma.firebaseinaction.presentation.ui.auth

data class AuthState(
    val isLoading: Boolean = false,
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
)