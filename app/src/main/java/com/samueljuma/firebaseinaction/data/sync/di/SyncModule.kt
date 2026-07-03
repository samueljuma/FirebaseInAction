package com.samueljuma.firebaseinaction.data.sync.di

import androidx.work.WorkManager
import com.samueljuma.firebaseinaction.data.sync.WorkManagerSyncScheduler
import com.samueljuma.firebaseinaction.domain.sync.SyncScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val syncModule = module {
    single { WorkManager.getInstance(androidContext()) }
    singleOf(::WorkManagerSyncScheduler).bind<SyncScheduler>()
}
