package com.samueljuma.firebaseinaction.domain.auth

import com.samueljuma.firebaseinaction.domain.auth.models.AuthSession

interface SessionStorage {
    suspend fun get(): AuthSession?
    suspend fun save(session: AuthSession)
    suspend fun clear()
}