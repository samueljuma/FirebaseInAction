package com.samueljuma.firebaseinaction.data.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.samueljuma.firebaseinaction.domain.auth.models.User
import com.samueljuma.firebaseinaction.domain.observability.CrashReporter

class FirebaseCrashReporter(
    private val crashlytics: FirebaseCrashlytics
) : CrashReporter {

    override fun setUser(user: User) {
        crashlytics.setUserId(user.uid)
        crashlytics.setCustomKey("is_email_verified", user.isEmailVerified)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun recordException(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    override fun setKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun clearUser() {
        crashlytics.setUserId("")
    }

    override fun setKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setKey(key: String, value: Long) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setKey(key: String, value: Float) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setKey(key: String, value: Double) {
        crashlytics.setCustomKey(key, value)
    }
}