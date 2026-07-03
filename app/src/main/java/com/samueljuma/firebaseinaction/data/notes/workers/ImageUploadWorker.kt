package com.samueljuma.firebaseinaction.data.notes.workers

import android.content.Context
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository
import com.samueljuma.firebaseinaction.domain.storage.UploadState
import timber.log.Timber

class ImageUploadWorker(
    context: Context,
    params: WorkerParameters,
    private val storageRepository: StorageRepository,
    private val noteRepository: NoteRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val noteId = inputData.getString(KEY_NOTE_ID)
            ?: return Result.failure()
        val uriString = inputData.getString(KEY_IMAGE_URI)
            ?: return Result.failure()

        Timber.tag(WORK_NAME).d("Starting image upload for note: $noteId")

        val uri = uriString.toUri()

        var uploadResult: Result = Result.failure()

        storageRepository.uploadNoteImageWithProgress(
            noteId = noteId,
            imageUri = uri
        ).collect { uploadState ->
            when (uploadState) {
                is UploadState.Progress -> {
                    setProgress(workDataOf(KEY_PROGRESS to uploadState.percentage))
                    Timber.tag(WORK_NAME).d("Upload progress: ${uploadState.percentage}%")
                }
                is UploadState.Success -> {
                    Timber.tag(WORK_NAME).d("Upload complete: ${uploadState.downloadUrl}")
                    val updateResult = noteRepository.updateNoteImageUrl(noteId, uploadState.downloadUrl)
                    uploadResult = if (updateResult is com.samueljuma.firebaseinaction.core.utils.Result.Success) {
                        Result.success(workDataOf(KEY_DOWNLOAD_URL to uploadState.downloadUrl))
                    } else {
                        Result.retry()
                    }
                }
                is UploadState.Error -> {
                    Timber.tag(WORK_NAME).e("Upload failed: ${uploadState.error}")
                    uploadResult = when (uploadState.error) {
                        DataError.Storage.NETWORK_ERROR -> Result.retry()
                        else -> Result.failure()
                    }
                }
            }
        }

        return uploadResult
    }

    companion object {
        const val WORK_NAME = "ImageUploadWorker"
        const val KEY_NOTE_ID = "note_id"
        const val KEY_IMAGE_URI = "image_uri"
        const val KEY_PROGRESS = "progress"
        const val KEY_DOWNLOAD_URL = "download_url"
    }
}