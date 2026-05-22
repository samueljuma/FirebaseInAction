package com.samueljuma.firebaseinaction.domain.auth

import com.samueljuma.firebaseinaction.core.utils.DataError
import kotlinx.coroutines.flow.Flow
import com.samueljuma.firebaseinaction.core.utils.Result

interface AuthRepository {
    val currentUser: Flow<User?>
    suspend fun signUp(email: String, password: String): Result<User, DataError.Auth>
    suspend fun signIn(email: String, password: String): Result<User, DataError.Auth>
    suspend fun signOut(): Result<Unit, DataError.Auth>
}