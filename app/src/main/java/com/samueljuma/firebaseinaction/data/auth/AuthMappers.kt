package com.samueljuma.firebaseinaction.data.auth

import com.samueljuma.firebaseinaction.domain.auth.models.User
import com.google.firebase.auth.FirebaseUser
import com.samueljuma.firebaseinaction.domain.auth.models.AuthSession

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

fun User.toSession() = AuthSession(
    uid = uid,
    email = email,
    displayName = displayName,
    isEmailVerified = isEmailVerified
)

fun AuthSession.toUser() = User(
    uid = uid,
    email = email,
    displayName = displayName,
    isEmailVerified = isEmailVerified
)