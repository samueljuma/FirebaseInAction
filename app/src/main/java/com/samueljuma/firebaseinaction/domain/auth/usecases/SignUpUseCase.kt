package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.models.User

class SignUpUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User, DataError.Auth> = repository.signUp(email, password)
}