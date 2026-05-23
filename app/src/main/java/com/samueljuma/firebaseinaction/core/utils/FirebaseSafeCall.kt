package com.samueljuma.firebaseinaction.core.utils

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.CancellationException
import timber.log.Timber

private const val TAG = "FirebaseSafeCall"

suspend fun <D> firebaseAuthSafeCall(
    block: suspend () -> D
): Result<D, DataError.Auth> = try {
    Result.Success(block())
} catch (e: CancellationException) {
    throw e  // Never swallow — coroutine cancellation must propagate
} catch (e: FirebaseAuthWeakPasswordException) {
    Timber.tag(TAG).w(e, "Weak password")
    Result.Error(DataError.Auth.WEAK_PASSWORD)
} catch (e: FirebaseAuthInvalidCredentialsException) {
    Timber.tag(TAG).w(e, "Invalid credentials")
    Result.Error(DataError.Auth.INVALID_CREDENTIALS)
} catch (e: FirebaseAuthUserCollisionException) {
    Timber.tag(TAG).w(e, "User already exists")
    Result.Error(DataError.Auth.USER_ALREADY_EXISTS)
} catch (e: FirebaseAuthInvalidUserException) {
    Timber.tag(TAG).w(e, "User not found")
    Result.Error(DataError.Auth.USER_NOT_FOUND)
} catch (e: FirebaseTooManyRequestsException) {
    Timber.tag(TAG).w(e, "Too many requests")
    Result.Error(DataError.Auth.TOO_MANY_REQUESTS)
} catch (e: FirebaseNetworkException) {
    Timber.tag(TAG).w(e, "Network error")
    Result.Error(DataError.Auth.NETWORK_ERROR)
} catch (e: Exception) {
    Timber.tag(TAG).e(e, "Unexpected auth error")
    Result.Error(DataError.Auth.UNKNOWN)
}


suspend fun <D> firestoreSafeCall(
    block: suspend () -> D
): Result<D, DataError.Firestore> = try {
    Result.Success(block())
} catch (e: FirebaseFirestoreException) {
    val error = when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED   -> DataError.Firestore.PERMISSION_DENIED
        FirebaseFirestoreException.Code.NOT_FOUND           -> DataError.Firestore.NOT_FOUND
        FirebaseFirestoreException.Code.ALREADY_EXISTS      -> DataError.Firestore.ALREADY_EXISTS
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED  -> DataError.Firestore.RESOURCE_EXHAUSTED
        FirebaseFirestoreException.Code.UNAVAILABLE         -> DataError.Firestore.NETWORK_ERROR
        else                                                -> DataError.Firestore.UNKNOWN
    }
    Result.Error(error)
} catch (e: FirebaseNetworkException) {
    Result.Error(DataError.Firestore.NETWORK_ERROR)
} catch (e: Exception) {
    if(e is CancellationException) throw e
    Result.Error(DataError.Firestore.UNKNOWN)
}


suspend fun <D> storageSafeCall(
    block: suspend () -> D
): Result<D, DataError.Storage> = try {
    Result.Success(block())
} catch (e: StorageException) {
    val error = when (e.errorCode) {
        StorageException.ERROR_OBJECT_NOT_FOUND  -> DataError.Storage.OBJECT_NOT_FOUND
        StorageException.ERROR_BUCKET_NOT_FOUND  -> DataError.Storage.BUCKET_NOT_FOUND
        StorageException.ERROR_NOT_AUTHORIZED    -> DataError.Storage.UNAUTHORIZED
        StorageException.ERROR_QUOTA_EXCEEDED    -> DataError.Storage.QUOTA_EXCEEDED
        StorageException.ERROR_PROJECT_NOT_FOUND -> DataError.Storage.UNKNOWN
        else                                     -> DataError.Storage.UNKNOWN
    }
    Result.Error(error)
} catch (e: FirebaseNetworkException) {
    Result.Error(DataError.Storage.NETWORK_ERROR)
} catch (e: Exception) {
    if(e is CancellationException) throw e
    Result.Error(DataError.Storage.UNKNOWN)
}