package com.samueljuma.firebaseinaction.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.firebaseAuthSafeCall
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.samueljuma.firebaseinaction.core.utils.Result
class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<User, DataError.Auth> = firebaseAuthSafeCall {
        val result = firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .await()
        result.user?.toUser() ?: error("User is null after sign up")
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<User, DataError.Auth> = firebaseAuthSafeCall {
        val result = firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .await()
        result.user?.toUser() ?: error("User is null after sign in")
    }

    override suspend fun signOut(): Result<Unit, DataError.Auth> = firebaseAuthSafeCall {
        firebaseAuth.signOut()
    }
}