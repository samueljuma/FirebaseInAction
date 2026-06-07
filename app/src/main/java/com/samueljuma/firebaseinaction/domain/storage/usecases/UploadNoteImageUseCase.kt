package com.samueljuma.firebaseinaction.domain.storage.usecases

import android.net.Uri
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository
import com.samueljuma.firebaseinaction.domain.storage.UploadState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UploadNoteImageUseCase(
    private val storageRepository: StorageRepository,
    private val noteRepository: NoteRepository,
    private val sessionStorage: SessionStorage
) {
    operator fun invoke(
        note: Note,
        imageUri: Uri
    ): Flow<UploadState> = flow {

        storageRepository.uploadNoteImageWithProgress(
            noteId = note.id,
            imageUri = imageUri
        ).collect { uploadState ->
            emit(uploadState)

            // When upload succeeds — update Firestore via repository
            if (uploadState is UploadState.Success) {
                noteRepository.updateNote(
                    note.copy(
                        imageUrl = uploadState.downloadUrl,
                        updatedAt = System.currentTimeMillis(),
                        isSynced = false
                    )
                )
            }
        }
    }
}