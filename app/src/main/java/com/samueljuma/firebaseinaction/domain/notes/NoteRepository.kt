package com.samueljuma.firebaseinaction.domain.notes

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotes(): Flow<List<Note>>
    suspend fun createNote(note: Note): Result<Unit, DataError>
    suspend fun updateNote(note: Note): Result<Unit, DataError>
    suspend fun deleteNote(noteId: String): Result<Unit, DataError>
    suspend fun syncNotes(): Result<Unit, DataError>
    fun startRemoteSync(): Flow<Unit>
}