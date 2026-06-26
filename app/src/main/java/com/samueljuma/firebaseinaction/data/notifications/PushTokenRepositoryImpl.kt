package com.samueljuma.firebaseinaction.data.notifications

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.firestoreSafeCall
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.notifications.PushTokenRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class PushTokenRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val sessionStorage: SessionStorage,
    private val firebaseMessaging: FirebaseMessaging
) : PushTokenRepository {

    override suspend fun getCurrentToken(): String? = try {
        return firebaseMessaging.token.await()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Timber.tag(TAG).e(e, "Failed to get FCM token")
        null
    }

    override suspend fun saveTokenForCurrentUser(
        token: String
    ): Result<Unit, DataError> = firestoreSafeCall {
        val userId = sessionStorage.get()?.uid
            ?: error("No authenticated user")

        // Store under the user's own document — only they can write it (Security Rules)
        firestore.document("users/$userId")
            .set(mapOf("fcmToken" to token), SetOptions.merge())
            .await()
    }

    companion object{
        const val TAG = "PushToken"
    }
}