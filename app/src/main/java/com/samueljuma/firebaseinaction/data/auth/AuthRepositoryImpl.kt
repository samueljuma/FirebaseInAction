package com.samueljuma.firebaseinaction.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.firebaseAuthSafeCall
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
import com.samueljuma.firebaseinaction.domain.logs.CrashReporter

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val sessionStorage: SessionStorage,
    private val crashReporter: CrashReporter,
    private val analyticsTracker: AnalyticsTracker
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override fun getCurrentUserSync(): User? = firebaseAuth.currentUser?.toUser()

    override suspend fun reloadCurrentUser(): Result<Unit, DataError.Auth> = firebaseAuthSafeCall {
        firebaseAuth.currentUser?.reload()?.await() ?: Unit
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<User, DataError.Auth> = firebaseAuthSafeCall {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()
        val user = result.user?.toUser() ?: error("User is null after sign up")
        sessionStorage.save(user.toSession())
        crashReporter.setUser(user)
        crashReporter.setKey("auth_method", "email")
        analyticsTracker.logEvent(
            AnalyticsEvent.SignUpCompleted(AnalyticsEvent.SignInMethod.EMAIL)
        )
        user

    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<User, DataError.Auth> = firebaseAuthSafeCall {
        val result = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
        val user = result.user?.toUser()
            ?: error("User is null after Google sign in")
        sessionStorage.save(user.toSession())
        crashReporter.setUser(user)
        crashReporter.setKey("auth_method", "email")
        analyticsTracker.logEvent(
            AnalyticsEvent.SignInCompleted(AnalyticsEvent.SignInMethod.EMAIL)
        )
        user
    }

    override suspend fun signOut(): Result<Unit, DataError.Auth> = firebaseAuthSafeCall {
        // Clear session BEFORE signing out of Firebase. firebaseAuth.signOut() fires
        // the AuthStateListener synchronously, and observeTokenExpiry() uses the presence
        // of a DataStore session to distinguish an explicit sign-out (session already gone)
        // from an unexpected token expiry (session still present). Reversing the order
        // would cause observeTokenExpiry() to incorrectly show the SessionExpiredDialog.
        sessionStorage.clear()
        crashReporter.clearUser()
        firebaseAuth.signOut()
    }

    override suspend fun signInWithGoogle(
        idToken: String
    ): Result<User, DataError.Auth> = firebaseAuthSafeCall {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user?.toUser() ?: error("User is null after Google sign in")
        sessionStorage.save(user.toSession())
        crashReporter.setUser(user)
        crashReporter.setKey("auth_method", "google")
        analyticsTracker.logEvent(
            AnalyticsEvent.SignInCompleted(AnalyticsEvent.SignInMethod.GOOGLE)
        )
        user
    }

    override suspend fun sendEmailVerification(): Result<Unit, DataError.Auth> =
        firebaseAuthSafeCall {
            // firebaseAuth.currentUser can be transiently null while Firebase restores
            // the session on startup or refreshes the ID token. Rather than reading the
            // sync property directly, we attach an AuthStateListener and wait for the
            // first non-null emission (fast path returns immediately if already set).
            val firebaseUser = firebaseAuth.currentUser
                ?: withTimeoutOrNull(5_000L) {
                    callbackFlow {
                        val listener = FirebaseAuth.AuthStateListener { auth ->
                            auth.currentUser?.let { trySend(it) }
                        }
                        firebaseAuth.addAuthStateListener(listener)
                        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
                    }.first()
                }
                ?: error("No current user")
            firebaseUser.sendEmailVerification().await()
        }
}