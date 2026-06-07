package com.samueljuma.firebaseinaction.domain.storage.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.core.utils.onError

class DeleteNoteImageUseCase(
    private val storageRepository: StorageRepository,
    private val noteRepository: NoteRepository,
) {
    suspend operator fun invoke(note: Note): Result<Unit, DataError> {
        // Delete from Storage first
        storageRepository.deleteNoteImage(note.id)
            .onError { return Result.Error(it) }

        // Then clear URL from Firestore
        return noteRepository.updateNote(
            note.copy(
                imageUrl = null,
                updatedAt = System.currentTimeMillis(),
                isSynced = false
            )
        )
    }
}