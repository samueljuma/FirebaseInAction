package com.samueljuma.firebaseinaction.data.config.di

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.samueljuma.firebaseinaction.BuildConfig
import com.samueljuma.firebaseinaction.data.config.FirebaseFeatureFlags
import com.samueljuma.firebaseinaction.domain.config.FeatureFlags
import org.koin.dsl.module

val remoteConfigModule = module {
    single { Firebase.remoteConfig }

    single<FeatureFlags> {
        FirebaseFeatureFlags(
            remoteConfig = get(),
            // No RC emulator: fetch immediately in debug for fast iteration, throttle to 1h in release.
            minimumFetchIntervalSeconds = if (BuildConfig.DEBUG) 0L else 3600L,
            // Process-lifetime scope: the live-update listener collects here until the app dies.
            scope = get(),
        )
    }
}