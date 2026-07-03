package com.samueljuma.firebaseinaction.data.observability

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsTracker
import timber.log.Timber

class FirebaseAnalyticsTracker(
    private val analytics: FirebaseAnalytics
) : AnalyticsTracker {

    override fun logEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            event.params.forEach { (key, value) ->
                when (value) {
                    is String  -> putString(key, value)
                    is Boolean -> putLong(key, if (value) 1L else 0L)
                    is Int     -> putInt(key, value)
                    is Long    -> putLong(key, value)
                    is Double  -> putDouble(key, value)
                    else       -> putString(key, value.toString())
                }
            }
        }
        analytics.logEvent(event.name, bundle)
        Timber.tag("Analytics").d("Event: ${event.name} params: ${event.params}")
    }

    override fun identify(userId: String) {
        analytics.setUserId(userId)
    }

    override fun reset() {
        analytics.setUserId(null)
    }
}
