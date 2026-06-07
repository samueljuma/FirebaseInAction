package com.samueljuma.firebaseinaction.data.notes.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE userId = :userId AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotes(userId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    fun getNoteById(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedNotes(userId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE userId = :userId")
    suspend fun getLocalNotes(userId: String): List<NoteEntity>

    @Upsert
    suspend fun upsertNote(note: NoteEntity)

    @Upsert
    suspend fun upsertNotes(notes: List<NoteEntity>)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNote(noteId: String)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun hardDeleteNote(noteId: String)

    @Query("UPDATE notes SET isDeleted = 1, isSynced = 0 WHERE id = :noteId")
    suspend fun softDeleteNote(noteId: String)

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :noteId")
    suspend fun markAsSynced(noteId: String)

    @Query("UPDATE notes SET imageUrl = :imageUrl, isSynced = 0 WHERE id = :noteId")
    suspend fun updateNoteImageUrl(noteId: String, imageUrl: String)
    @Transaction
    suspend fun upsertRemoteNotes(remoteNotes: List<NoteEntity>, userId: String) {
        remoteNotes.forEach { remoteNote ->
            val localNote = getNoteByIdSync(remoteNote.id)

            when {
                // Note deleted locally — don't re-insert from remote
                localNote?.isDeleted == true -> {
                    Timber.tag("NoteDao").d("Skipping re-insert of deleted note: ${remoteNote.id}")
                }
                // Note has unsynced local changes — don't overwrite
                localNote?.isSynced == false -> {
                    Timber.tag("NoteDao").d("Skipping remote update for unsynced note: ${remoteNote.id}")
                }
                // Safe to apply remote version
                else -> upsertNote(remoteNote)
            }
        }
    }
    @Query("SELECT * FROM notes WHERE id = :noteId LIMIT 1")
    suspend fun getNoteByIdSync(noteId: String): NoteEntity?
}