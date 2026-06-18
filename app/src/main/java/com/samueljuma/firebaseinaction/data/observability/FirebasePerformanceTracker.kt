package com.samueljuma.firebaseinaction.data.observability

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import com.samueljuma.firebaseinaction.domain.observability.PerformanceTracker
import com.samueljuma.firebaseinaction.domain.observability.TraceHandle

class FirebasePerformanceTracker(
    private val performance: FirebasePerformance
) : PerformanceTracker {

    override fun startTrace(name: String): TraceHandle {
        val trace = performance.newTrace(name)
        trace.start()
        return FirebaseTraceHandle(trace)
    }
}

class FirebaseTraceHandle(
    private val trace: Trace
) : TraceHandle {
    override fun putAttribute(key: String, value: String) {
        trace.putAttribute(key, value)
    }

    override fun putMetric(key: String, value: Long) {
        trace.putMetric(key, value)
    }

    override fun stop() {
        trace.stop()
    }
}