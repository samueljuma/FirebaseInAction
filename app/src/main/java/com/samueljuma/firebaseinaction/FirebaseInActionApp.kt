package com.samueljuma.firebaseinaction

import android.app.Application
import com.samueljuma.firebaseinaction.core.di.appModules
import com.samueljuma.firebaseinaction.core.emulator.FirebaseEmulatorConfig
import com.samueljuma.firebaseinaction.core.logging.CrashReportingTree
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import timber.log.Timber

class FirebaseInActionApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Timber first — so emulator logs appear
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        else Timber.plant(CrashReportingTree())

        // Emulator config second — before Firebase is used anywhere
        FirebaseEmulatorConfig.configure()


        startKoin {
            androidContext(this@FirebaseInActionApp)
            workManagerFactory()
            modules(appModules)
        }
    }
}