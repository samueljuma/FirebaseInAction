package com.samueljuma.firebaseinaction.domain.notifications

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result

interface PushTokenRepository {
    suspend fun getCurrentToken(): String?
    suspend fun saveTokenForCurrentUser(token: String): Result<Unit, DataError>
}