package com.samueljuma.firebaseinaction.data.auth

import kotlinx.serialization.Serializable
/*
* For future Use
 */
@Serializable
data class UserDto(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val isEmailVerified: Boolean
)