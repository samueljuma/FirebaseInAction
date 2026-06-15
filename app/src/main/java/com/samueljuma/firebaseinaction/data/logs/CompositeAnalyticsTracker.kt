package com.samueljuma.firebaseinaction.data.logs

import com.samueljuma.firebaseinaction.domain.logs.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker

class CompositeAnalyticsTracker(
    private val trackers: List<AnalyticsTracker>
) : AnalyticsTracker {
    override fun logEvent(event: AnalyticsEvent) = trackers.forEach { it.logEvent(event) }
    override fun identify(userId: String) = trackers.forEach { it.identify(userId) }
    override fun reset() = trackers.forEach { it.reset() }
}