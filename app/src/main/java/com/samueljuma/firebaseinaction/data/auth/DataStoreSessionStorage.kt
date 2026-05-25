package com.samueljuma.firebaseinaction.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.auth.models.AuthSession
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber

class DataStoreSessionStorage(
    private val context: Context
) : SessionStorage {

    private val Context.dataStore by preferencesDataStore(
        name = "auth_session"
    )

    private object Keys {
        val UID = stringPreferencesKey("uid")
        val EMAIL = stringPreferencesKey("email")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val IS_EMAIL_VERIFIED = booleanPreferencesKey("is_email_verified")
    }

    override suspend fun get(): AuthSession? {
        return context.dataStore.data
            .catch { exception ->
                Timber.tag(TAG).e(exception, "Error reading session")
                emit(emptyPreferences())
            }
            .map { prefs ->
                val uid = prefs[Keys.UID] ?: return@map null
                AuthSession(
                    uid = uid,
                    email = prefs[Keys.EMAIL],
                    displayName = prefs[Keys.DISPLAY_NAME],
                    isEmailVerified = prefs[Keys.IS_EMAIL_VERIFIED] ?: false
                )
            }
            .first()
    }

    override suspend fun save(session: AuthSession) {
        context.dataStore.edit { prefs ->
            prefs[Keys.UID] = session.uid
            session.email?.let { prefs[Keys.EMAIL] = it }
            session.displayName?.let { prefs[Keys.DISPLAY_NAME] = it }
            prefs[Keys.IS_EMAIL_VERIFIED] = session.isEmailVerified
        }
    }

    override suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        const val TAG = "SessionStorage"
    }
}