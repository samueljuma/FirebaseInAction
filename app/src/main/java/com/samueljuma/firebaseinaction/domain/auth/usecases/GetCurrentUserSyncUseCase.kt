package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.models.User

class GetCurrentUserSyncUseCase(
    private val repository: AuthRepository
) {
    operator fun invoke(): User? = repository.getCurrentUserSync()
}