package com.samueljuma.firebaseinaction.core.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber


class CrashReportingTree : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        // Ignore debug and verbose logs in production entirely
        if (priority == Log.DEBUG || priority == Log.VERBOSE) return

        // For warnings and errors — send to Crashlytics
        // We'll flesh this out fully in the Crashlytics milestone
        t?.let {
            FirebaseCrashlytics.getInstance().recordException(it)
        }
    }
}