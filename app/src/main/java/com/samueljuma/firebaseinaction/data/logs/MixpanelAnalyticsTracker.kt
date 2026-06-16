package com.samueljuma.firebaseinaction.data.logs

import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
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
        mixpanel.flush()
        Timber.tag("Analytics").d("Event: ${event.name} params: ${event.params}")
    }

    override fun identify(userId: String) {
        mixpanel.identify(userId)
    }

    override fun reset() {
        mixpanel.reset()
    }
}