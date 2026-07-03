package com.samueljuma.firebaseinaction.core.di

import com.samueljuma.firebaseinaction.FirebaseInActionApp
import kotlinx.coroutines.CoroutineScope
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * Application-lifetime [CoroutineScope] for fire-and-forget work that must outlive a single
 * screen/ViewModel (e.g. flushing pending "read" writes on back-navigation, warming Remote Config).
 * SupervisorJob so one failed child never tears the scope down.
 *
 * The scope is **owned by** [FirebaseInActionApp] and merely exposed here. Registered unqualified
 * because the app only ever has one such scope — which lets the constructor DSL auto-wire it by type.
 */
val coreModule = module {
    single<CoroutineScope> {
        (androidApplication() as FirebaseInActionApp).applicationScope
    }
}