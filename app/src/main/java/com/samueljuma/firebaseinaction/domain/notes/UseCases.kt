package com.samueljuma.firebaseinaction.domain.notes

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import kotlinx.coroutines.flow.Flow
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.util.IdGenerator

class GetNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
}

class GetNoteByIdUseCase(private val repository: NoteRepository) {
    operator fun invoke(noteId: String): Flow<Note?> =
        repository.getNoteById(noteId)
}

class DeleteNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(noteId: String): Result<Unit, DataError> =
        repository.deleteNote(noteId)
}

class UpdateNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Result<Unit, DataError> =
        repository.updateNote(note)
}

class StartRemoteSyncUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<Unit> = repository.startRemoteSync()
}

class CreateNoteUseCase(
    private val repository: NoteRepository,
    private val idGenerator: IdGenerator,
    private val sessionStorage: SessionStorage
) {
    suspend operator fun invoke(
        title: String,
        content: String
    ): Result<Unit, DataError> {
        val userId = sessionStorage.get()?.uid
            ?: return Result.Error(DataError.Auth.UNKNOWN)

        val note = Note(
            id = idGenerator.generate(),
            userId = userId,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            isPinned = false,
            isSynced = false
        )

        return repository.createNote(note)
    }
}