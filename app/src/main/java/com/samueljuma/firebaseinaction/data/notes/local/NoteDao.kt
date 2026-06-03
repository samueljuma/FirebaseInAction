package com.samueljuma.firebaseinaction.data.notes.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY isPinned DESC, updatedAt DESC")
    fun getNotes(userId: String): Flow<List<NoteEntity>>

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

    @Query("UPDATE notes SET isSynced = 1 WHERE id = :noteId")
    suspend fun markAsSynced(noteId: String)

    suspend fun upsertRemoteNotes(notes: List<NoteEntity>, userId: String) {
        val localNotes = getLocalNotes(userId)
        val unsyncedIds = localNotes.filter { !it.isSynced }.map { it.id }.toSet()
        val localById = localNotes.associateBy { it.id }
        upsertNotes(
            notes.filter { note ->
                note.id !in unsyncedIds &&
                    (localById[note.id]?.updatedAt ?: Long.MIN_VALUE) <= note.updatedAt
            }
        )
    }
}