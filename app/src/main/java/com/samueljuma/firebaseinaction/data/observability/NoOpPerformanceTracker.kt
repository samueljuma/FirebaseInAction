package com.samueljuma.firebaseinaction.data.observability

import com.samueljuma.firebaseinaction.domain.observability.PerformanceTracker
import com.samueljuma.firebaseinaction.domain.observability.TraceHandle

class NoOpPerformanceTracker : PerformanceTracker {
    override fun startTrace(name: String): TraceHandle = NoOpTraceHandle()
}

class NoOpTraceHandle : TraceHandle {
    override fun putAttribute(key: String, value: String) = Unit
    override fun putMetric(key: String, value: Long) = Unit
    override fun stop() = Unit
}