package com.samueljuma.firebaseinaction.domain.notes

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import kotlinx.coroutines.flow.Flow
import com.samueljuma.firebaseinaction.core.utils.Result

class GetNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
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

class CreateNoteUseCase(private val repository: NoteRepository) {
    suspend operator fun invoke(note: Note): Result<Unit, DataError> =
        repository.createNote(note)
}