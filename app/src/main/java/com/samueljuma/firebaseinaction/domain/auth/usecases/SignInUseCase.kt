package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.models.User

class SignInUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        email: String,
        password: String
    ): Result<User, DataError.Auth> {
        if (email.isBlank()) return Result.Error(DataError.Auth.INVALID_EMAIL)
        if (password.isBlank()) return Result.Error(DataError.Auth.INVALID_PASSWORD)
        return repository.signIn(email, password)
    }
}