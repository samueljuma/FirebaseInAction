package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.auth.models.AuthSession

class GetSessionUseCase(
    private val sessionStorage: SessionStorage
) {
    suspend operator fun invoke(): AuthSession? = sessionStorage.get()
}