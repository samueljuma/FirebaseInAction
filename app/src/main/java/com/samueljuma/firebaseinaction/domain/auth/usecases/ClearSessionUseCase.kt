package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.domain.auth.SessionStorage

class ClearSessionUseCase(
    private val sessionStorage: SessionStorage
) {
    suspend operator fun invoke() = sessionStorage.clear()
}