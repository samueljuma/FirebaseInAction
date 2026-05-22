package com.samueljuma.firebaseinaction.domain.auth

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isEmailVerified: Boolean
)
