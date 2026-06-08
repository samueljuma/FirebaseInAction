package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.notes.DeleteNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNoteByIdUseCase
import com.samueljuma.firebaseinaction.domain.notes.UpdateNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import com.samueljuma.firebaseinaction.domain.storage.UploadState
import com.samueljuma.firebaseinaction.domain.storage.usecases.DeleteNoteImageUseCase
import com.samueljuma.firebaseinaction.domain.storage.usecases.UploadNoteImageUseCase
import com.samueljuma.firebaseinaction.domain.sync.SyncScheduler
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val uploadNoteImageUseCase: UploadNoteImageUseCase,
    private val deleteNoteImageUseCase: DeleteNoteImageUseCase,
    private val syncScheduler: SyncScheduler
) : MviViewModel<NoteDetailState, NoteDetailAction, NoteDetailEvent>(
    NoteDetailState()
) {

    private val noteId: String = checkNotNull(savedStateHandle["noteId"])

    init {
        observeNote()
    }

    private fun observeNote() {
        viewModelScope.launch {
            getNoteByIdUseCase(noteId)
                .filterNotNull()                    // skip null emissions
                .distinctUntilChanged()             // skip if note unchanged
                .filter { !state.value.isEditing }  // skip while user is typing
                .collect { note ->
                    updateState {
                        copy(
                            noteId = note.id,
                            userId = note.userId,
                            title = note.title,
                            content = note.content,
                            isPinned = note.isPinned,
                            isSynced = note.isSynced,
                            isLoading = false,
                            createdAt = note.createdAt,
                            lastUpdated = note.updatedAt,
                            imageUrl = note.imageUrl
                        )
                    }
                }
        }
    }

    override fun onAction(action: NoteDetailAction) {
        when (action) {
            NoteDetailAction.OnBackClicked -> {
                if (state.value.isUploadingImage) {
                    updateState { copy(showCancelUploadDialog = true) }
                } else {
                    emitEvent(NoteDetailEvent.NavigateBack)
                }
            }
            NoteDetailAction.OnCancelUploadConfirmed -> {
                updateState { copy(showCancelUploadDialog = false) }
                syncScheduler.cancelImageUpload(noteId)
                emitEvent(NoteDetailEvent.NavigateBack)
            }
            NoteDetailAction.OnCancelUploadDismissed ->
                updateState { copy(showCancelUploadDialog = false) }
            NoteDetailAction.OnEditClicked -> updateState { copy(isEditing = true) }
            NoteDetailAction.OnSaveClicked -> saveNote()
            NoteDetailAction.OnDeleteClicked -> deleteNote()
            NoteDetailAction.OnPinClicked -> pinNote()
            is NoteDetailAction.OnTitleChanged ->
                updateState { copy(title = action.title) }
            is NoteDetailAction.OnContentChanged ->
                updateState { copy(content = action.content) }
            NoteDetailAction.OnImageClicked ->  emitEvent(NoteDetailEvent.LaunchImagePicker)
            is NoteDetailAction.OnImageSelected -> uploadImage(action.uri)
            NoteDetailAction.OnRemoveImageClicked -> removeImage()
        }
    }

    private fun saveNote() {
        viewModelScope.launch {
            updateState { copy(isSaving = true) }

            updateNoteUseCase(buildNoteFromState())
                .onSuccess { updateState { copy(isEditing = false, isSaving = false) } }
                .onError { error ->
                    emitEvent(NoteDetailEvent.ShowSnackbar(error.toUiText()))
                    updateState { copy(isSaving = false) }
                }
        }
    }

    private fun deleteNote() {
        viewModelScope.launch {
            deleteNoteUseCase(noteId)
                .onSuccess { emitEvent(NoteDetailEvent.NavigateBack) }
                .onError { error ->
                    emitEvent(NoteDetailEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun pinNote() {
        viewModelScope.launch {
            updateNoteUseCase(buildNoteFromState(isPinned = !state.value.isPinned))
                .onError { error ->
                    emitEvent(NoteDetailEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    // Builds a Note from current state with overrides
    private fun buildNoteFromState(
        isPinned: Boolean = state.value.isPinned,
        title: String = state.value.title,
        content: String = state.value.content
    ): Note = Note(
        id = state.value.noteId,
        userId = state.value.userId,
        title = title.trim(),
        content = content.trim(),
        createdAt = state.value.createdAt,
        updatedAt = System.currentTimeMillis(),
        isPinned = isPinned,
        isSynced = false,
        imageUrl = state.value.imageUrl
    )

    private fun uploadImage(uri: Uri) {
        viewModelScope.launch {
            val note = buildNoteFromState()
            updateState { copy(isUploadingImage = true, uploadProgress = 0) }

            uploadNoteImageUseCase(note, uri)
                .collect { uploadState ->
                    when (uploadState) {
                        is UploadState.Progress -> {
                            updateState { copy(uploadProgress = uploadState.percentage) }
                        }
                        is UploadState.Success -> {
                            updateState {
                                copy(
                                    imageUrl = uploadState.downloadUrl,
                                    isUploadingImage = false,
                                    uploadProgress = null
                                )
                            }
                        }
                        is UploadState.Error -> {
                            emitEvent(
                                NoteDetailEvent.ShowSnackbar(
                                    uploadState.error.toUiText()
                                )
                            )
                            if (uploadState.error == DataError.Storage.NETWORK_ERROR) {
                                syncScheduler.scheduleImageUpload(noteId, uri.toString())
                            }
                            updateState {
                                copy(isUploadingImage = false, uploadProgress = null)
                            }
                        }
                    }
                }
        }
    }

    private fun removeImage() {
        viewModelScope.launch {
            deleteNoteImageUseCase(buildNoteFromState())
                .onSuccess {
                    updateState { copy(imageUrl = null) }
                }
                .onError { error ->
                    emitEvent(NoteDetailEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

}