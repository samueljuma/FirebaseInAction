package com.samueljuma.firebaseinaction.domain.notes

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import kotlinx.coroutines.flow.Flow
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
import com.samueljuma.firebaseinaction.domain.notes.validator.NoteValidator

class GetNotesUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> = repository.getNotes()
}

class GetNoteByIdUseCase(private val repository: NoteRepository) {
    operator fun invoke(noteId: String): Flow<Note?> =
        repository.getNoteById(noteId)
}

class DeleteNoteUseCase(
    private val repository: NoteRepository,
    private val analyticsTracker: AnalyticsTracker
) {
    suspend operator fun invoke(
        noteId: String,
        noteCreatedAt: Long,
        hadImage: Boolean
    ): Result<Unit, DataError> {
        return repository.deleteNote(noteId).also { result ->
            if(result is Result.Success){
                val ageMs = System.currentTimeMillis() - noteCreatedAt
                val ageDays = ageMs / (1000 * 60 * 60 * 24)
                analyticsTracker.logEvent(
                    AnalyticsEvent.NoteDeleted(
                        hadImage = hadImage,
                        noteAgeDays = ageDays
                    )
                )
            }
        }
    }

}

class UpdateNoteUseCase(
    private val repository: NoteRepository,
    private val noteValidator: NoteValidator
) {
    suspend operator fun invoke(note: Note): Result<Unit, DataError> {
        noteValidator.validate(note.title, note.content)
            .onError { return Result.Error(it) }
        return repository.updateNote(note)
    }

}

class StartRemoteSyncUseCase(private val repository: NoteRepository) {
    operator fun invoke(): Flow<Unit> = repository.startRemoteSync()
}

class CreateNoteUseCase(
    private val repository: NoteRepository,
    private val sessionStorage: SessionStorage,
    private val noteValidator: NoteValidator,
    private val analyticsTracker: AnalyticsTracker
) {
    suspend operator fun invoke(
        id: String,
        title: String,
        content: String,
        imageUrl: String? = null
    ): Result<Unit, DataError> {

        noteValidator.validate(title, content)
            .onError { return Result.Error(it) }

        val userId = sessionStorage.get()?.uid
            ?: return Result.Error(DataError.Auth.UNKNOWN)

        val note = Note(
            id = id,
            userId = userId,
            title = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            pinned = false,
            synced = false,
            imageUrl = imageUrl
        )

        return repository.createNote(note).also { result ->
            if(result is Result.Success){
                analyticsTracker.logEvent(
                    AnalyticsEvent.NoteSaved(hasImage = imageUrl != null)
                )
            }
        }
    }
}
