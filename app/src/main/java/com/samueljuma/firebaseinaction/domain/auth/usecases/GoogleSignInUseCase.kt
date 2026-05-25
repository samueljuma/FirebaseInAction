package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.models.User

class GoogleSignInUseCase(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(idToken: String): Result<User, DataError.Auth> =
        repository.signInWithGoogle(idToken)
}