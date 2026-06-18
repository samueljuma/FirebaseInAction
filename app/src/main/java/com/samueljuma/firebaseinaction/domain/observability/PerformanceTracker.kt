package com.samueljuma.firebaseinaction.domain.observability

import java.io.Closeable

interface PerformanceTracker {
    fun startTrace(name: String): TraceHandle
}

interface TraceHandle : Closeable {
    fun putAttribute(key: String, value: String)
    fun putMetric(key: String, value: Long)
    fun stop()
    override fun close() = stop()  // Closeable contract calls stop()
}