package com.samueljuma.firebaseinaction.data.logs

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.samueljuma.firebaseinaction.BuildConfig
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
import com.samueljuma.firebaseinaction.domain.logs.CrashReporter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val logsModule = module {
    //Crashlytics
    single {
        FirebaseCrashlytics.getInstance().also {
            it.isCrashlyticsCollectionEnabled = !BuildConfig.USE_EMULATOR
        }
    }
    singleOf(::FirebaseCrashReporter).bind<CrashReporter>()

    //Analytics
    single {
        FirebaseAnalytics.getInstance(androidContext()).also {
            it.setAnalyticsCollectionEnabled(!BuildConfig.USE_EMULATOR)
        }
    }
    singleOf(::FirebaseAnalyticsTracker).bind<AnalyticsTracker>()
}