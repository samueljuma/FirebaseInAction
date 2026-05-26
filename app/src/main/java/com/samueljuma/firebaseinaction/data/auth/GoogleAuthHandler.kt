package com.samueljuma.firebaseinaction.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.googleSignInSafeCall
import timber.log.Timber

class GoogleAuthHandler(
    private val context: Context,
    private val webClientId: String
) {
    suspend fun getGoogleIdToken(): Result<String, DataError.Auth> = googleSignInSafeCall {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(
            request = request,
            context = context
        )

        val googleIdTokenCredential = GoogleIdTokenCredential
            .createFrom(result.credential.data)
        Timber.tag(TAG).i("Google ID token retrieved successfully")
        googleIdTokenCredential.idToken
    }

    companion object {
        const val TAG = "GoogleAuth"
    }
}