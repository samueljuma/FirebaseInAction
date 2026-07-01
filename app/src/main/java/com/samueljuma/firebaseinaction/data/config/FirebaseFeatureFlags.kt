package com.samueljuma.firebaseinaction.data.config

import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.samueljuma.firebaseinaction.domain.config.AppConfig
import com.samueljuma.firebaseinaction.domain.config.FeatureFlags
import com.samueljuma.firebaseinaction.domain.config.RemoteConfigKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.tasks.await
import timber.log.Timber

/**
 * Firebase-backed [FeatureFlags] — the only class in the app that touches the Remote Config SDK.
 *
 * Loading strategy (per Firebase guidance):
 *  - **Warm start value:** the SDK persists *activated* values on disk, so `init` reads them straight
 *    back via [toAppConfig] — the last-known real config is available instantly, offline, with no
 *    flash of defaults. (We do **not** add our own SharedPrefs/Room cache; that would just duplicate
 *    the SDK's store.) [AppConfig.Defaults] is only the cold-start / first-run / fetch-failed fallback.
 *  - **Fresh values:** [sync] fetches + activates in the background for this session and the next.
 *  - **Live values:** [configUpdates] streams server-pushed changes so the app reacts without a restart.
 *
 * Both the one-shot [sync] and the streaming listener write into a single [MutableStateFlow] sink,
 * which backs the public [config]. Remote Config has **no local emulator**, so this always hits the
 * live backend regardless of flavor — hence the debug fetch interval of 0 (instant experimentation).
 */
class FirebaseFeatureFlags(
    private val remoteConfig: FirebaseRemoteConfig,
    minimumFetchIntervalSeconds: Long,
    scope: CoroutineScope,
) : FeatureFlags {

    private val _config = MutableStateFlow(AppConfig.Defaults)
    override val config: StateFlow<AppConfig> = _config.asStateFlow()

    init {
        remoteConfig.setConfigSettingsAsync(
            remoteConfigSettings {
                minimumFetchIntervalInSeconds = minimumFetchIntervalSeconds
            }
        )
        remoteConfig.setDefaultsAsync(AppConfig.Defaults.toDefaultsMap())
        // Publish whatever was activated in a previous session before the first fetch completes.
        _config.value = remoteConfig.toAppConfig()

        // Push-based freshness: re-emit whenever the backend publishes a new version.
        remoteConfig.configUpdates()
            .onEach {
                _config.value = remoteConfig.toAppConfig()
                Timber.tag(TAG).d("Live update → ${_config.value}")
            }
            .launchIn(scope)
    }

    override suspend fun sync() {
        runCatching { remoteConfig.fetchAndActivate().await() }
            .onSuccess { activated ->
                _config.value = remoteConfig.toAppConfig()
                Timber.tag(TAG).d("Synced (changed=$activated) → ${_config.value}")
            }
            .onFailure { e ->
                Timber.tag(TAG).w(e, "Sync failed — retaining ${_config.value}")
            }
    }

    companion object {
        private const val TAG = "FeatureFlags"
    }
}

/**
 * The real-time config-update listener as a cold [Flow]. Each emission means "a new template was
 * activated" — collectors re-read [toAppConfig]. [awaitClose] removes the SDK registration when the
 * collecting scope is cancelled, so there is no leaked listener.
 */
private fun FirebaseRemoteConfig.configUpdates(): Flow<Unit> = callbackFlow {
    val registration = addOnConfigUpdateListener(object : ConfigUpdateListener {
        override fun onUpdate(configUpdate: ConfigUpdate) {
            // A new version exists but isn't live yet — activate, then signal collectors.
            activate().addOnSuccessListener { trySend(Unit) }
        }

        override fun onError(error: FirebaseRemoteConfigException) {
            Timber.tag("FeatureFlags").w(error, "Live update listener error")
        }
    })
    awaitClose { registration.remove() }
}

/** Decodes the currently-active Remote Config values into the domain [AppConfig] type. */
private fun FirebaseRemoteConfig.toAppConfig() = AppConfig(
    inAppBannerEnabled = getBoolean(RemoteConfigKeys.ENABLE_IN_APP_BANNER),
    maxFreeNotes = getLong(RemoteConfigKeys.MAX_FREE_NOTES),
    welcomeMessage = getString(RemoteConfigKeys.WELCOME_MESSAGE),
)

/** Projects the typed defaults into the `Map<String, Any>` the SDK expects for `setDefaultsAsync`. */
private fun AppConfig.toDefaultsMap(): Map<String, Any> = mapOf(
    RemoteConfigKeys.ENABLE_IN_APP_BANNER to inAppBannerEnabled,
    RemoteConfigKeys.MAX_FREE_NOTES to maxFreeNotes,
    RemoteConfigKeys.WELCOME_MESSAGE to welcomeMessage,
)
