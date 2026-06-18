package com.samueljuma.firebaseinaction.data.notes.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetSessionUseCase
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.observability.PerformanceTracker
import timber.log.Timber

class NoteSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val noteRepository: NoteRepository,
    private val sessionStorage: SessionStorage,
    private val performanceTracker: PerformanceTracker
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Timber.tag("NoteSyncWorker").d("Worker started")
        val uid = sessionStorage.get()?.uid
            ?: return Result.failure()

        Timber.tag(WORK_NAME).d("Starting note sync for user: $uid")

        return performanceTracker.startTrace("note_sync").use { trace ->
            trace.putAttribute("user_id", uid)

            noteRepository.syncNotes()
                .let { result ->
                    when (result) {
                        is com.samueljuma.firebaseinaction.core.utils.Result.Success -> {
                            Timber.tag(WORK_NAME).d("Note sync completed")
                            Result.success()
                        }
                        is com.samueljuma.firebaseinaction.core.utils.Result.Error -> {
                            trace.putAttribute("error", result.error.toString())
                            Timber.tag(WORK_NAME).e("Note sync failed: ${result.error}")
                            when (result.error) {
                                DataError.Firestore.NETWORK_ERROR -> Result.retry()
                                else -> Result.failure()
                            }
                        }
                    }
                }
        }
    }

    companion object {
        const val WORK_NAME = "NoteSyncWorker"
    }
}