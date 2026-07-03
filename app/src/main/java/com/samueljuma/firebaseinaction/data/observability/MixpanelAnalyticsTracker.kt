package com.samueljuma.firebaseinaction.data.observability

import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsTracker
import org.json.JSONObject
import timber.log.Timber

class MixpanelAnalyticsTracker(
    private val mixpanel: MixpanelAPI
) : AnalyticsTracker {

    override fun logEvent(event: AnalyticsEvent) {
        val props = JSONObject().apply {
            event.params.forEach { (key, value) -> put(key, value) }
        }
        mixpanel.track(event.name, props)
        Timber.tag("Analytics").d("Event: ${event.name} params: ${event.params}")
    }

    override fun identify(userId: String) {
        mixpanel.identify(userId)
    }

    override fun reset() {
        mixpanel.flush() // send buffered events before reset
        mixpanel.reset()
    }
}