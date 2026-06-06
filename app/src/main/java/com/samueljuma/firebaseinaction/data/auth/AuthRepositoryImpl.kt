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
import kotlinx.coroutines.tasks.await
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth,
    private val sessionStorage: SessionStorage
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
        user
    }

    override suspend fun signOut(): Result<Unit, DataError.Auth> = firebaseAuthSafeCall {
        firebaseAuth.signOut()
        sessionStorage.clear()
    }

    override suspend fun signInWithGoogle(
        idToken: String
    ): Result<User, DataError.Auth> = firebaseAuthSafeCall {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user?.toUser() ?: error("User is null after Google sign in")
        sessionStorage.save(user.toSession())
        user
    }

    override suspend fun sendEmailVerification(): Result<Unit, DataError.Auth> =
        firebaseAuthSafeCall {
            firebaseAuth.currentUser?.sendEmailVerification()?.await()
                ?: error("No current user")
        }
}