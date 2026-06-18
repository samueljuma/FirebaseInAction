package com.samueljuma.firebaseinaction.domain.observability

interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun identify(userId: String) {}
    fun reset() {}
}

