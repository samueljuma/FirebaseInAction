package com.samueljuma.firebaseinaction.domain.storage.usecases

import android.net.Uri
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository
import com.samueljuma.firebaseinaction.domain.storage.UploadState
import kotlinx.coroutines.flow.Flow

class UploadImageOnlyUseCase(private val storageRepository: StorageRepository) {
    operator fun invoke(noteId: String, imageUri: Uri): Flow<UploadState> =
        storageRepository.uploadNoteImageWithProgress(noteId, imageUri)
}
