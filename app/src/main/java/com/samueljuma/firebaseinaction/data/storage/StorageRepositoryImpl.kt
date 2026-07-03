package com.samueljuma.firebaseinaction.data.storage

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.storageSafeCall
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.observability.PerformanceTracker
import com.samueljuma.firebaseinaction.domain.storage.UploadState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber


class StorageRepositoryImpl(
    private val storage: FirebaseStorage,
    private val sessionStorage: SessionStorage,
    private val performanceTracker: PerformanceTracker
) : StorageRepository {

    private suspend fun getCurrentUserId(): String =
        sessionStorage.get()?.uid ?: error("No authenticated user")

    override fun uploadNoteImageWithProgress(
        noteId: String,
        imageUri: Uri
    ): Flow<UploadState> = callbackFlow {
        val userId = try {
            getCurrentUserId()
        } catch (e: Exception) {
            trySend(UploadState.Error(DataError.Storage.UNAUTHORIZED))
            close()
            return@callbackFlow
        }

        val trace = performanceTracker.startTrace("image_upload")
        trace.putAttribute("note_id", noteId)


        val ref = storage.reference
            .child("users/$userId/notes/$noteId/cover.jpg")

        val uploadTask = ref.putFile(imageUri)

        // Progress listener
        uploadTask.addOnProgressListener { snapshot ->
            val progress = (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount)
            trySend(UploadState.Progress(progress.toInt()))
        }

        // Success listener
        uploadTask.addOnSuccessListener {
            // Get download URL after successful upload
            ref.downloadUrl
                .addOnSuccessListener { uri ->
                    val sizeKb = (uploadTask.snapshot.totalByteCount) / 1024
                    trace.putMetric("file_size_kb", sizeKb)
                    trace.stop()  //  explicit stop on success

                    trySend(UploadState.Success(uri.toString()))
                    close()
                }
                .addOnFailureListener { e ->
                    Timber.tag("Storage").e(e, "Failed to get download URL")
                    trace.putAttribute("error", "download_url_failed")
                    trace.stop()  //  explicit stop on failure
                    trySend(UploadState.Error(DataError.Storage.UNKNOWN))
                    close()
                }
        }

        // Failure listener
        uploadTask.addOnFailureListener { e ->
            Timber.tag("Storage").e(e, "Upload failed")
            val error = if (e is StorageException) {
                when (e.errorCode) {
                    StorageException.ERROR_OBJECT_NOT_FOUND -> DataError.Storage.OBJECT_NOT_FOUND
                    StorageException.ERROR_BUCKET_NOT_FOUND -> DataError.Storage.BUCKET_NOT_FOUND
                    StorageException.ERROR_NOT_AUTHORIZED   -> DataError.Storage.UNAUTHORIZED
                    StorageException.ERROR_QUOTA_EXCEEDED   -> DataError.Storage.QUOTA_EXCEEDED
                    StorageException.ERROR_PROJECT_NOT_FOUND -> DataError.Storage.UNKNOWN
                    else -> DataError.Storage.UNKNOWN
                }
            } else {
                DataError.Storage.NETWORK_ERROR
            }
            trace.putAttribute("error", error.toString())
            trace.stop()  //  explicit stop on failure
            trySend(UploadState.Error(error))
            close()
        }

        // Cancel upload if flow is cancelled
        awaitClose {
            if (!uploadTask.isComplete) {
                trace.putAttribute("cancelled", "true")
                trace.stop()  // stop on cancellation too
                uploadTask.cancel()
            }
            Timber.tag("Storage").d("Upload cancelled")
        }
    }

    override suspend fun deleteNoteImage(
        noteId: String,
    ): Result<Unit, DataError.Storage> = storageSafeCall {
        val userId = getCurrentUserId()
        storage.reference
            .child("users/$userId/notes/$noteId/cover.jpg")
            .delete()
            .await()
    }
}