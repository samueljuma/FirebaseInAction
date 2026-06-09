package com.samueljuma.firebaseinaction.core.emulator

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.storage.storage
import com.samueljuma.firebaseinaction.BuildConfig
import timber.log.Timber

object FirebaseEmulatorConfig {

    fun configure() {
        if (!BuildConfig.USE_EMULATOR) return

        val host = BuildConfig.EMULATOR_HOST

        Timber.tag("Emulator").d("Connecting to Firebase Emulators at $host")

        // Auth Emulator
        Firebase.auth.useEmulator(host, 9099)
        Timber.tag("Emulator").d("Auth → $host:9099")

        // Firestore Emulator
        Firebase.firestore.useEmulator(host, 8080)
        Firebase.firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = false  // disable offline cache for clean test state
        }
        Timber.tag("Emulator").d("Firestore → $host:8080")

        // Storage Emulator
        Firebase.storage.useEmulator(host, 9199)
        Timber.tag("Emulator").d("Storage → $host:9199")
    }
}