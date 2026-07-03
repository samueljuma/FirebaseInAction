package com.samueljuma.firebaseinaction.domain.storage.usecases

import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository

class DeleteImageOnlyUseCase(private val storageRepository: StorageRepository) {
    suspend operator fun invoke(noteId: String): Result<Unit, DataError.Storage> =
        storageRepository.deleteNoteImage(noteId)
}
