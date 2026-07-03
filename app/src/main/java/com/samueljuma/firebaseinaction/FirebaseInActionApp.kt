package com.samueljuma.firebaseinaction

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.samueljuma.firebaseinaction.core.di.appModules
import com.samueljuma.firebaseinaction.core.emulator.FirebaseEmulatorConfig
import com.samueljuma.firebaseinaction.core.lifecycle.AppForegroundTracker
import com.samueljuma.firebaseinaction.core.logging.CrashReportingTree
import com.samueljuma.firebaseinaction.core.notifications.NotificationChannels
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.config.FeatureFlags
import com.samueljuma.firebaseinaction.domain.notifications.PushTokenRepository
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsTracker
import com.samueljuma.firebaseinaction.domain.observability.CrashReporter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import timber.log.Timber

class FirebaseInActionApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        // 0. App Check FIRST — before anything touches a Firebase service (emulator config,
        // Koin-built repositories, the FCM token resync below), so every request carries an
        // attestation token. Debug builds use the Debug provider (prints a token to logcat that
        // must be registered in the console); release builds attest via Play Integrity.
        FirebaseAppCheck.getInstance().apply {
            installAppCheckProviderFactory(
                if (BuildConfig.DEBUG) DebugAppCheckProviderFactory.getInstance()
                else PlayIntegrityAppCheckProviderFactory.getInstance()
            )
            // Explicit: auto-refresh otherwise follows the Analytics collection setting,
            // which this project toggles per flavor (disabled under USE_EMULATOR).
            setTokenAutoRefreshEnabled(true)
        }

        AppForegroundTracker.init()
        NotificationChannels.createChannels(this)

        // 1. Timber first — logging available for everything that follows
        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
        else Timber.plant(CrashReportingTree())

        // 2. Emulator config — before any Firebase SDK use
        FirebaseEmulatorConfig.configure()

        // 3. Crashlytics collection — after emulator config
        // USE_EMULATOR = true  (devDebug/devRelease) → disable
        // USE_EMULATOR = false (prodRelease)          → enable
        // FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = !BuildConfig.USE_EMULATOR // This is handles in logsModule by koin

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
            ?.let {
                koin.get<CrashReporter>().setUser(it)
                koin.get<AnalyticsTracker>().identify(it.uid)

                // Re-sync the FCM token for this returning user. signUp/signIn/signInWithGoogle
                // only capture the token at the moment of that explicit call — a persisted
                // session (the common case on relaunch) never re-executes those, so without this
                // the token in Firestore can silently go missing/stale and pushes stop arriving.
                applicationScope.launch {
                    val pushTokenRepository = koin.get<PushTokenRepository>()
                    pushTokenRepository.getCurrentToken()?.let { token ->
                        pushTokenRepository.saveTokenForCurrentUser(token)
                    }
                }
            }

        // 6. Warm Remote Config — resolving FeatureFlags applies in-app defaults, publishes any
        // previously-activated (cached) values, and registers the live-update listener. Then fetch
        // the latest in the background: applied live this session, and cached for the next launch.
        val featureFlags = koin.get<FeatureFlags>()
        applicationScope.launch { featureFlags.sync() }
    }
}