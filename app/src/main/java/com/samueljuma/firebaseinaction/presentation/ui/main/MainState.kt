package com.samueljuma.firebaseinaction.presentation.ui.main

import com.samueljuma.firebaseinaction.domain.notifications.model.AppNotification

data class MainState(
    val isCheckingAuth: Boolean = true,
    val isLoggedIn: Boolean = false,
    val isEmailVerified: Boolean = false,
    val sessionExpired: Boolean = false,
    val activeNotification: AppNotification? = null
)