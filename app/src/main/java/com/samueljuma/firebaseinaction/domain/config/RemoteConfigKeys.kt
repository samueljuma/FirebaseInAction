package com.samueljuma.firebaseinaction.domain.config

/**
 * Canonical Remote Config parameter keys.
 *
 * These strings must match the parameter names published in `remote_config.json` / the Firebase
 * console **exactly** — they are the contract between the backend template and the app.
 */
object RemoteConfigKeys {
    const val ENABLE_IN_APP_BANNER = "enable_in_app_banner"
    const val MAX_FREE_NOTES = "max_free_notes"
    const val WELCOME_MESSAGE = "welcome_message"
}
