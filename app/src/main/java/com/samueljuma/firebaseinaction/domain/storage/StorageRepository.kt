package com.samueljuma.firebaseinaction.domain.storage

import android.net.Uri
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.Result
import kotlinx.coroutines.flow.Flow


interface StorageRepository {
    fun uploadNoteImageWithProgress(
        noteId: String,
        imageUri: Uri
    ): Flow<UploadState>

    suspend fun deleteNoteImage(
        noteId: String,
    ): Result<Unit, DataError.Storage>
}