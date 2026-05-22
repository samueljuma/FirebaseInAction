package com.samueljuma.firebaseinaction.data.auth

import com.samueljuma.firebaseinaction.domain.auth.User
import com.google.firebase.auth.FirebaseUser
fun UserDto.toUser() = User(
    uid = uid,
    email = email,
    displayName = displayName,
    isEmailVerified = isEmailVerified
)
fun FirebaseUser.toUser(): User = User(
    uid = uid,
    email = email,
    displayName = displayName,
    isEmailVerified = isEmailVerified
)