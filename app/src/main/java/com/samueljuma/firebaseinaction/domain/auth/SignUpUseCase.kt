package com.samueljuma.firebaseinaction.domain.auth

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User, DataError.Auth> = repository.signUp(email, password)
}