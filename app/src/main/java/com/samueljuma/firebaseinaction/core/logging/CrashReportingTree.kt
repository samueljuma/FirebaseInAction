package com.samueljuma.firebaseinaction.core.logging

import android.util.Log
import com.samueljuma.firebaseinaction.domain.logs.CrashReporter
import org.koin.core.context.GlobalContext
import timber.log.Timber

class CrashReportingTree : Timber.Tree() {

    // Lazy — Koin is not yet started when Timber.plant() is called in Application.onCreate().
    // By the time any log() fires, Koin is fully initialized and the get() is safe.
    private val crashReporter: CrashReporter by lazy { GlobalContext.get().get() }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Skip debug and verbose — too noisy for crash reports
        if (priority == Log.DEBUG || priority == Log.VERBOSE) return

        // Set key-value context visible in Crashlytics dashboard
        tag?.let { crashReporter.setKey("timber_tag", it) }
        crashReporter.setKey("log_priority", priorityLabel(priority))

        // Log the message as a breadcrumb — shows what happened before the crash
        crashReporter.log("[${priorityLabel(priority)}] ${tag ?: "NoTag"}: $message")

        // Record actual exceptions — these show as non-fatal issues
        t?.let { crashReporter.recordException(it) }
    }

    private fun priorityLabel(priority: Int): String = when (priority) {
        Log.INFO   -> "INFO"
        Log.WARN   -> "WARN"
        Log.ERROR  -> "ERROR"
        Log.ASSERT -> "ASSERT"
        else       -> "UNKNOWN"
    }
}
