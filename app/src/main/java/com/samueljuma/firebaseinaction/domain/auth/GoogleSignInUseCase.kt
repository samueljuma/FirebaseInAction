package com.samueljuma.firebaseinaction.domain.auth

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result

class GoogleSignInUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User, DataError.Auth> =
        repository.signInWithGoogle(idToken)
}