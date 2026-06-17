package com.samueljuma.firebaseinaction.data.logs

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.samueljuma.firebaseinaction.BuildConfig
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
import com.samueljuma.firebaseinaction.domain.logs.CrashReporter
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val logsModule = module {
    // Crashlytics
    single {
        FirebaseCrashlytics.getInstance().also {
            it.isCrashlyticsCollectionEnabled = !BuildConfig.USE_EMULATOR
        }
    }
    singleOf(::FirebaseCrashReporter).bind<CrashReporter>()

    // Firebase Analytics
    single {
        FirebaseAnalytics.getInstance(androidContext()).also {
            it.setAnalyticsCollectionEnabled(!BuildConfig.USE_EMULATOR)
        }
    }
    single { FirebaseAnalyticsTracker(get()) }

    // Mixpanel — opt out when running against the emulator so dev noise stays out of prod data
    single {
        MixpanelAPI.getInstance(androidContext(), BuildConfig.MIXPANEL_TOKEN, false).also {
            if (BuildConfig.USE_EMULATOR) it.optOutTracking()
            it.setServerURL("https://api-eu.mixpanel.com")
        }
    }
    single { MixpanelAnalyticsTracker(get()) }

    // Composite tracker — events fan out to both Firebase and Mixpanel
    single<AnalyticsTracker> {
        CompositeAnalyticsTracker(
            listOf(get<FirebaseAnalyticsTracker>(), get<MixpanelAnalyticsTracker>())
        )
    }
}
