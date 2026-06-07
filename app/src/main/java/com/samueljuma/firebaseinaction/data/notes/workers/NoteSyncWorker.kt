package com.samueljuma.firebaseinaction.data.notes.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetSessionUseCase
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import timber.log.Timber

class NoteSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val noteRepository: NoteRepository,
    private val getSessionUseCase: GetSessionUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.tag("NoteSyncWorker").d("Worker started")
        val session = getSessionUseCase()
            ?: return Result.failure() // No session — nothing to sync

        Timber.tag(WORK_NAME).d("Starting note sync for user: ${session.uid}")

        return noteRepository.syncNotes()
            .onSuccess {
                Timber.tag(WORK_NAME).d("Note sync completed successfully")
            }
            .onError { error ->
                Timber.tag(WORK_NAME).e("Note sync failed: $error")
            }
            .let { result ->
                when (result) {
                    is com.samueljuma.firebaseinaction.core.utils.Result.Success ->
                        Result.success()
                    is com.samueljuma.firebaseinaction.core.utils.Result.Error ->
                        when (result.error) {
                            DataError.Firestore.NETWORK_ERROR -> Result.retry() // retry on network
                            else -> Result.failure() // don't retry on other errors
                        }
                }
            }
    }

    companion object {
        const val WORK_NAME = "NoteSyncWorker"
    }
}