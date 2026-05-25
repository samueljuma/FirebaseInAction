package com.samueljuma.firebaseinaction.domain.auth.usecases

import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.models.User
import kotlinx.coroutines.flow.Flow

class GetCurrentUserUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<User?> = repository.currentUser
}