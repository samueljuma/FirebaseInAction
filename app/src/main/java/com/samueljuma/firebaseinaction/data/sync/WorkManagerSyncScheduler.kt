package com.samueljuma.firebaseinaction.data.sync

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.samueljuma.firebaseinaction.data.notes.workers.ImageUploadWorker
import com.samueljuma.firebaseinaction.data.notes.workers.NoteSyncWorker
import com.samueljuma.firebaseinaction.domain.sync.SyncScheduler
import java.util.concurrent.TimeUnit

class WorkManagerSyncScheduler(
    private val workManager: WorkManager
) : SyncScheduler {

    override fun scheduleNotesSync() {
        val request = OneTimeWorkRequestBuilder<NoteSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        workManager.enqueueUniqueWork(
            NoteSyncWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun scheduleImageUpload(noteId: String, imageUri: String) {
        val inputData = workDataOf(
            ImageUploadWorker.KEY_NOTE_ID to noteId,
            ImageUploadWorker.KEY_IMAGE_URI to imageUri
        )
        val request = OneTimeWorkRequestBuilder<ImageUploadWorker>()
            .setInputData(inputData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        workManager.enqueueUniqueWork(
            "${ImageUploadWorker.WORK_NAME}_$noteId",
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    override fun cancelImageUpload(noteId: String) {
        workManager.cancelUniqueWork("${ImageUploadWorker.WORK_NAME}_$noteId")
    }

    override fun cancelAllSyncs() {
        workManager.cancelAllWork()
    }
}
