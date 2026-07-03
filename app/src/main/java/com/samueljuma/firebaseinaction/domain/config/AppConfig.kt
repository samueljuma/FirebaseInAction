package com.samueljuma.firebaseinaction.domain.config

/**
 * Strongly-typed snapshot of everything the app allows to be tuned remotely.
 *
 * Modelling the flags as a single immutable value object — rather than scattering raw
 * `getBoolean(...)` / `getLong(...)` calls across the codebase — keeps consumers depending on
 * domain types instead of the Remote Config SDK (Dependency Inversion), and makes [Defaults] the
 * one source of truth for baseline behaviour before the first fetch ever lands.
 */
data class AppConfig(
    val inAppBannerEnabled: Boolean,
    val maxFreeNotes: Long,
    val welcomeMessage: String,
) {
    companion object {
        /** In-app defaults; also the initial value emitted by [FeatureFlags.config]. */
        val Defaults = AppConfig(
            inAppBannerEnabled = true,
            maxFreeNotes = 50L,
            welcomeMessage = "",
        )
    }
}
