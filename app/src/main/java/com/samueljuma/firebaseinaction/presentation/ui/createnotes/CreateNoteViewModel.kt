package com.samueljuma.firebaseinaction.presentation.ui.createnotes

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.utils.UiText
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.logs.AnalyticsTracker
import com.samueljuma.firebaseinaction.domain.notes.CreateNoteUseCase
import com.samueljuma.firebaseinaction.domain.storage.UploadState
import com.samueljuma.firebaseinaction.domain.storage.usecases.DeleteImageOnlyUseCase
import com.samueljuma.firebaseinaction.domain.storage.usecases.UploadImageOnlyUseCase
import com.samueljuma.firebaseinaction.domain.sync.SyncScheduler
import com.samueljuma.firebaseinaction.domain.util.IdGenerator
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateNoteViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val uploadImageOnlyUseCase: UploadImageOnlyUseCase,
    private val deleteImageOnlyUseCase: DeleteImageOnlyUseCase,
    private val idGenerator: IdGenerator,
    private val syncScheduler: SyncScheduler,
    private val analyticsTracker: AnalyticsTracker
) : MviViewModel<CreateNoteState, CreateNoteAction, CreateNoteEvent>(CreateNoteState()) {

    init {
        // Fire immediately when screen opens
        analyticsTracker.logEvent(AnalyticsEvent.NoteCreateStarted)
    }
    private val pendingNoteId: String = idGenerator.generate()
    private var noteSaved = false

    override fun onAction(action: CreateNoteAction) {
        when (action) {
            is CreateNoteAction.OnTitleChanged ->
                updateState { copy(title = action.title) }
            is CreateNoteAction.OnContentChanged ->
                updateState { copy(content = action.content) }
            CreateNoteAction.OnSaveClicked -> saveNote()
            CreateNoteAction.OnBackClicked -> {
                if (state.value.isUploadingImage) {
                    updateState { copy(showCancelUploadDialog = true) }
                } else {
                    cleanupAndNavigateBack()
                }
            }
            CreateNoteAction.OnImageClicked ->
                emitEvent(CreateNoteEvent.LaunchImagePicker)
            is CreateNoteAction.OnImageSelected -> uploadImage(action.uri)
            CreateNoteAction.OnRemoveImageClicked ->
                updateState { copy(imageUrl = null) }
            CreateNoteAction.OnCancelUploadConfirmed -> {
                updateState { copy(showCancelUploadDialog = false) }
                cleanupAndNavigateBack()
            }
            CreateNoteAction.OnCancelUploadDismissed ->
                updateState { copy(showCancelUploadDialog = false) }
        }
    }

    private fun cleanupAndNavigateBack() {
        if (noteSaved) {
            emitEvent(CreateNoteEvent.NavigateBack)
            return
        }
        syncScheduler.cancelImageUpload(pendingNoteId)

        val imageUrl = state.value.imageUrl
        if (imageUrl != null) {
            viewModelScope.launch {
                deleteImageOnlyUseCase(pendingNoteId)
                    .onError { Timber.tag(TAG).e("Failed to delete orphaned image: $it") }
                emitEvent(CreateNoteEvent.NavigateBack)
            }
        } else {
            emitEvent(CreateNoteEvent.NavigateBack)
        }
    }

    private fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            updateState { copy(isUploadingImage = true, uploadProgress = 0) }
            uploadImageOnlyUseCase(pendingNoteId, uri).collect { uploadState ->
                when (uploadState) {
                    is UploadState.Progress ->
                        updateState { copy(uploadProgress = uploadState.percentage) }
                    is UploadState.Success ->{
                        // Log when image is added in create flow
                        analyticsTracker.logEvent(
                            AnalyticsEvent.NoteImageAdded(AnalyticsEvent.ImageSource.CREATE)
                        )
                        updateState {
                            copy(
                                imageUrl = uploadState.downloadUrl,
                                isUploadingImage = false,
                                uploadProgress = null
                            )
                        }
                    }
                    is UploadState.Error -> {
                        emitEvent(CreateNoteEvent.ShowSnackbar(uploadState.error.toUiText()))
                        if (uploadState.error == DataError.Storage.NETWORK_ERROR) {
                            syncScheduler.scheduleImageUpload(pendingNoteId, uri.toString())
                        }
                        updateState { copy(isUploadingImage = false, uploadProgress = null) }
                    }
                }
            }
        }
    }

    private fun saveNote() {
        val title = state.value.title.trim()
        val content = state.value.content.trim()

        if (title.isBlank()) {
            emitEvent(CreateNoteEvent.ShowSnackbar(
                UiText.StringResource(R.string.error_title_empty)
            ))
            return
        }

        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            createNoteUseCase(
                id = pendingNoteId,
                title = title,
                content = content,
                imageUrl = state.value.imageUrl
            )
                .onSuccess {
                    noteSaved = true
                    emitEvent(CreateNoteEvent.NavigateBack)
                }
                .onError { error ->
                    emitEvent(CreateNoteEvent.ShowSnackbar(error.toUiText()))
                }

            updateState { copy(isSaving = false) }
        }
    }

    companion object {
        private const val TAG = "CreateNoteViewModel"
    }
}
