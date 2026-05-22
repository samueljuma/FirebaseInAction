package com.samueljuma.firebaseinaction.domain.auth

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result

class SignOutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): Result<Unit, DataError.Auth> = repository.signOut()
}