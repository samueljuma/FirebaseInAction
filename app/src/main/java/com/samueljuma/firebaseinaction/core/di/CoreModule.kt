package com.samueljuma.firebaseinaction.core.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * Application-lifetime CoroutineScope for fire-and-forget work that must outlive
 * a single screen/ViewModel (e.g. flushing pending "read" writes on back-navigation).
 * SupervisorJob so one failed child never tears the scope down.
 */
val APPLICATION_SCOPE = named("applicationScope")

val coreModule = module {
    single(APPLICATION_SCOPE) {
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }
}
