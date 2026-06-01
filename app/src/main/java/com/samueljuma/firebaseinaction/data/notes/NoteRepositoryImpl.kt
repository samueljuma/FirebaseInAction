package com.samueljuma.firebaseinaction.data.notes

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.data.notes.local.NoteDao
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.notes.mapper.toNote
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.firestoreSafeCall
import com.samueljuma.firebaseinaction.data.notes.remote.NoteDto
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.notes.mapper.toDto
import com.samueljuma.firebaseinaction.domain.notes.mapper.toEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val firestore: FirebaseFirestore,
    private val workManager: WorkManager,
    private val sessionStorage: SessionStorage
) : NoteRepository {

    private suspend fun getCurrentUserId(): String =
        sessionStorage.get()?.uid ?: error("No authenticated user")

    override fun getNotes(): Flow<List<Note>> = flow {
        val userId = getCurrentUserId()
        emitAll(
            noteDao.getNotes(userId)
                .map { entities -> entities.map { it.toNote() } }
        )
    }

    override suspend fun createNote(note: Note): Result<Unit, DataError> {
        return try {
            noteDao.upsertNote(note.toEntity().copy(isSynced = false))
            enqueueSyncWork()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to create note locally")
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit, DataError> {
        return try {
            noteDao.upsertNote(
                note.toEntity().copy(
                    updatedAt = System.currentTimeMillis(),
                    isSynced = false
                )
            )
            enqueueSyncWork()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to update note locally")
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit, DataError> {
        return try {
            val userId = getCurrentUserId()
            noteDao.deleteNote(noteId)
            firestoreSafeCall {
                firestore.document("users/$userId/notes/$noteId")
                    .delete()
                    .await()
            }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to delete note")
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun syncNotes(): Result<Unit, DataError> {
        val userId = getCurrentUserId()
        return firestoreSafeCall {
            val unsyncedNotes = noteDao.getUnsyncedNotes(userId)
            Timber.tag(TAG).d("Syncing ${unsyncedNotes.size} unsynced notes")
            coroutineScope {
                unsyncedNotes.map { entity ->
                    async {
                        firestore
                            .document("users/$userId/notes/${entity.id}")
                            .set(entity.toDto())
                            .await()
                        noteDao.markAsSynced(entity.id)
                        Timber.tag(TAG).d("Synced note: ${entity.id}")
                    }
                }.awaitAll()
            }
        }
    }

    override fun startRemoteSync(): Flow<Unit> = flow {
        val userId = getCurrentUserId()
        emitAll(
            callbackFlow {
                val listener = firestore
                    .collection("users/$userId/notes")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Timber.tag(TAG).e(error, "Firestore listener error")
                            return@addSnapshotListener
                        }
                        snapshot?.documents?.let { documents ->
                            CoroutineScope(Dispatchers.IO).launch {
                                val entities = documents.mapNotNull { doc ->
                                    doc.toObject(NoteDto::class.java)?.toEntity()
                                }
                                noteDao.upsertRemoteNotes(entities, userId)
                                Timber.tag(TAG).d(
                                    "Remote sync: processed ${entities.size} remote notes"
                                )
                            }
                        }
                        trySend(Unit)
                    }
                awaitClose {
                    Timber.tag(TAG).d("Stopping Firestore listener")
                    listener.remove()
                }
            }
        )
    }

    private fun enqueueSyncWork() {
        Timber.tag(TAG).d("Enqueuing sync work")
        val syncRequest = OneTimeWorkRequestBuilder<NoteSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            NoteSyncWorker.WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            syncRequest
        )
    }

    companion object {
        const val TAG = "NoteRepository"
    }
}