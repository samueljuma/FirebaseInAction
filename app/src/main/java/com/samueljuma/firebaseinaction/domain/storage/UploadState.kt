package com.samueljuma.firebaseinaction.domain.storage

import com.samueljuma.firebaseinaction.core.utils.DataError

sealed interface UploadState {
    data class Progress(val percentage: Int) : UploadState
    data class Success(val downloadUrl: String) : UploadState
    data class Error(val error: DataError.Storage) : UploadState
}