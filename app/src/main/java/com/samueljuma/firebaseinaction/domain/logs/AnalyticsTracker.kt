package com.samueljuma.firebaseinaction.domain.logs

interface AnalyticsTracker {
    fun logEvent(event: AnalyticsEvent)
    fun identify(userId: String) {}
    fun reset() {}
}

