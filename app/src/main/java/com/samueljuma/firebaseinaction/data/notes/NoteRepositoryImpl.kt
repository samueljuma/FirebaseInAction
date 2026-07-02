package com.samueljuma.firebaseinaction.data.notes

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
import com.samueljuma.firebaseinaction.domain.observability.CrashReporter
import com.samueljuma.firebaseinaction.domain.notes.mapper.toDto
import com.samueljuma.firebaseinaction.domain.notes.mapper.toEntity
import com.samueljuma.firebaseinaction.domain.observability.PerformanceTracker
import com.samueljuma.firebaseinaction.domain.sync.SyncScheduler
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
import kotlin.coroutines.cancellation.CancellationException

class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val firestore: FirebaseFirestore,
    private val syncScheduler: SyncScheduler,
    private val sessionStorage: SessionStorage,
    private val crashReporter: CrashReporter,
    private val performanceTracker: PerformanceTracker
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

    override fun getNoteById(noteId: String): Flow<Note?> = flow {
        emitAll(
            noteDao.getNoteById(noteId)
                .map { it?.toNote() }
        )
    }

    override suspend fun createNote(note: Note): Result<Unit, DataError> {
        crashReporter.log("Creating note locally: ${note.id}")
        return try {
            noteDao.upsertNote(note.toEntity().copy(synced = false))
            syncScheduler.scheduleNotesSync()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to create note locally")
            crashReporter.recordException(e)
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun updateNote(note: Note): Result<Unit, DataError> {
        return try {
            noteDao.updateNoteFields(
                noteId = note.id,
                title = note.title,
                content = note.content,
                pinned = note.pinned,
                updatedAt = System.currentTimeMillis(),
                imageUrl = note.imageUrl,
                reminderAt = note.reminderAt,
                reminderFiredAt = note.reminderFiredAt
            )
            syncScheduler.scheduleNotesSync()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to update note locally")
            crashReporter.recordException(e)
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun updateNoteImageUrl(
        noteId: String,
        imageUrl: String
    ): Result<Unit, DataError> {
        return try {
            noteDao.updateNoteImageUrl(noteId, imageUrl)
            syncScheduler.scheduleNotesSync()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to update image URL for note $noteId")
            crashReporter.recordException(e)
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun deleteNote(noteId: String): Result<Unit, DataError> {
        crashReporter.log("Deleting note: $noteId")
        return try {
            noteDao.softDeleteNote(noteId)
            syncScheduler.scheduleNotesSync()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to delete note")
            crashReporter.recordException(e)
            Result.Error(DataError.Local.UNKNOWN)
        }
    }

    override suspend fun syncNotes(): Result<Unit, DataError> {
        return firestoreSafeCall {
            val userId = getCurrentUserId()
            performanceTracker.startTrace("firestore_fetch_notes").use { trace ->
                val unsyncedNotes = noteDao.getUnsyncedNotes(userId)
                trace.putMetric("unsynced_count", unsyncedNotes.size.toLong())
                crashReporter.log("Syncing ${unsyncedNotes.size} unsynced notes")
                crashReporter.setKey("unsynced_note_count", unsyncedNotes.size)
                Timber.tag(TAG).d("Syncing ${unsyncedNotes.size} unsynced notes")
                coroutineScope {
                    unsyncedNotes.map { entity ->
                        async {
                            if (entity.isDeleted) {
                                // Push deletion to Firestore
                                firestore.document("users/$userId/notes/${entity.id}")
                                    .delete()
                                    .await()
                                // Hard delete from Room — no longer needed
                                noteDao.hardDeleteNote(entity.id)
                                Timber.tag(TAG).d("Synced deletion: ${entity.id}")
                            } else {
                                // Push update/create to Firestore
                                firestore.document("users/$userId/notes/${entity.id}")
                                    .set(entity.toDto())
                                    .await()
                                noteDao.markAsSynced(entity.id)
                                Timber.tag(TAG).d("Synced note: ${entity.id}")
                            }
                        }

                    }.awaitAll()
                }
                crashReporter.log("Sync complete")
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
                            launch(Dispatchers.IO) {
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

    companion object {
        const val TAG = "NoteRepository"
    }
}