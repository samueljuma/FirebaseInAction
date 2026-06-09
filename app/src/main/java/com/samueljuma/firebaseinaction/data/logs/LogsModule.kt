package com.samueljuma.firebaseinaction.data.logs

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.samueljuma.firebaseinaction.domain.logs.CrashReporter
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val logsModule = module {
    single { FirebaseCrashlytics.getInstance() }
    singleOf(::FirebaseCrashReporter).bind<CrashReporter>()
}