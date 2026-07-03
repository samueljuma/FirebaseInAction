package com.samueljuma.firebaseinaction.presentation.ui.auth.emailverification

data class EmailVerificationState(
    val isCheckingVerification: Boolean = false,
    val isResending: Boolean = false,
    val email: String = ""
)