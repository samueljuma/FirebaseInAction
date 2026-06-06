package com.samueljuma.firebaseinaction.presentation.ui.main

data class MainState(
    val isCheckingAuth: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val sessionExpired: Boolean = false
)