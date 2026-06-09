package com.samueljuma.firebaseinaction

import android.app.Application
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.samueljuma.firebaseinaction.core.di.appModules
import com.samueljuma.firebaseinaction.core.emulator.FirebaseEmulatorConfig
import com.samueljuma.firebaseinaction.core.logging.CrashReportingTree
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.logs.CrashReporter
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import timber.log.Timber

class FirebaseInActionApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Timber first — logging available for everything that follows
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        else Timber.plant(CrashReportingTree())

        // 2. Emulator config — before any Firebase SDK use
        FirebaseEmulatorConfig.configure()

        // 3. Crashlytics collection — after emulator config
        // USE_EMULATOR = true  (devDebug/devRelease) → disable
        // USE_EMULATOR = false (prodRelease)          → enable
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.USE_EMULATOR

        Timber.d("Crashlytics enabled: ${!BuildConfig.USE_EMULATOR}")

        // 4. Koin last — repositories initialized here use Firebase
        startKoin {
            androidContext(this@FirebaseInActionApp)
            workManagerFactory()
            modules(appModules)
        }

        // 5. Restore Crashlytics user context for returning users — if the app is killed
        // and relaunched while a session is active, Firebase Auth still has the user but
        // Crashlytics loses the user ID. Re-tagging here ensures no anonymous crash reports.
        val koin = GlobalContext.get()
        koin.get<AuthRepository>().getCurrentUserSync()
            ?.let { koin.get<CrashReporter>().setUser(it) }
    }
}