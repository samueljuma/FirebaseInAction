package com.samueljuma.firebaseinaction.presentation.ui.auth.signup

data class SignUpState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false
)
