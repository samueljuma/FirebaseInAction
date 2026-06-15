package com.samueljuma.firebaseinaction.domain.logs

import com.samueljuma.firebaseinaction.domain.auth.models.User

interface CrashReporter {
    fun setUser(user: User)
    fun clearUser()
    fun log(message: String)
    fun recordException(throwable: Throwable)
    fun setKey(key: String, value: String)
    fun setKey(key: String, value: Boolean)
    fun setKey(key: String, value: Int)
    fun setKey(key: String, value: Long)
    fun setKey(key: String, value: Float)
    fun setKey(key: String, value: Double)
}