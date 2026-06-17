package com.samueljuma.firebaseinaction.data.logs

import com.samueljuma.firebaseinaction.domain.logs.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
import timber.log.Timber

class CompositeAnalyticsTracker(
    private val trackers: List<AnalyticsTracker>
) : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) {
        trackers.forEach { tracker ->
            runCatching { tracker.logEvent(event) }
                .onFailure { Timber.tag("Analytics").e(it, "Tracker ${tracker::class.simpleName} failed for event ${event.name}") }
        }
    }

    override fun identify(userId: String) {
        trackers.forEach { tracker ->
            runCatching { tracker.identify(userId) }
                .onFailure { Timber.tag("Analytics").e(it, "Tracker ${tracker::class.simpleName} failed to identify user") }
        }
    }

    override fun reset() {
        trackers.forEach { tracker ->
            runCatching { tracker.reset() }
                .onFailure { Timber.tag("Analytics").e(it, "Tracker ${tracker::class.simpleName} failed to reset") }
        }
    }
}