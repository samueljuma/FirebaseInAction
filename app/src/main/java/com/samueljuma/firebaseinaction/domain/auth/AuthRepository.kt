package com.samueljuma.firebaseinaction.domain.auth

import com.samueljuma.firebaseinaction.core.utils.DataError
import kotlinx.coroutines.flow.Flow
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.models.User

interface AuthRepository {
    val currentUser: Flow<User?>
    fun getCurrentUserSync(): User?
    suspend fun signUp(email: String, password: String): Result<User, DataError.Auth>
    suspend fun signIn(email: String, password: String): Result<User, DataError.Auth>
    suspend fun signOut(): Result<Unit, DataError.Auth>
    suspend fun signInWithGoogle(idToken: String): Result<User, DataError.Auth>
    suspend fun reloadCurrentUser(): Result<Unit, DataError.Auth>
    suspend fun sendEmailVerification(): Result<Unit, DataError.Auth>
}