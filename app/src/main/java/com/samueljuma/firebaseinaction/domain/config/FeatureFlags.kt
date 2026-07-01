package com.samueljuma.firebaseinaction.domain.config

import kotlinx.coroutines.flow.StateFlow

/**
 * Read-only, reactive gateway to remotely-controlled configuration.
 *
 * The rest of the app depends only on this abstraction — never on Firebase — satisfying the
 * Dependency Inversion Principle and keeping the SDK swappable and fakeable in tests.
 */
interface FeatureFlags {

    /**
     * Latest resolved configuration. Seeded with [AppConfig.Defaults] and updated after every
     * successful activate — including live, server-pushed updates.
     */
    val config: StateFlow<AppConfig>

    /**
     * Fetches the newest values from the backend and activates them. Safe to call repeatedly;
     * failures are swallowed so the last-known [config] is retained.
     */
    suspend fun sync()
}