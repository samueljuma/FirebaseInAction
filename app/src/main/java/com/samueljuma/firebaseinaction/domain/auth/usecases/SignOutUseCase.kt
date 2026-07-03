package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit, DataError.Auth> = repository.signOut()
}