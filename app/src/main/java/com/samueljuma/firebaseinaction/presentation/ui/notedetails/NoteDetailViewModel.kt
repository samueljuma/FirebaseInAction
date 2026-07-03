package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.utils.UiText
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsEvent
import com.samueljuma.firebaseinaction.domain.observability.AnalyticsTracker
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
import java.util.Calendar
import java.util.TimeZone

class NoteDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val getNoteByIdUseCase: GetNoteByIdUseCase,
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val uploadNoteImageUseCase: UploadNoteImageUseCase,
    private val deleteNoteImageUseCase: DeleteNoteImageUseCase,
    private val syncScheduler: SyncScheduler,
    private val analyticsTracker: AnalyticsTracker
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
                            pinned = note.pinned,
                            synced = note.synced,
                            isLoading = false,
                            createdAt = note.createdAt,
                            lastUpdated = note.updatedAt,
                            imageUrl = note.imageUrl,
                            reminderAt = note.reminderAt,
                            reminderFiredAt = note.reminderFiredAt
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
            NoteDetailAction.OnSetReminderClicked ->
                updateState { copy(showDatePicker = true) }
            is NoteDetailAction.OnReminderDateSelected ->
                updateState {
                    copy(
                        showDatePicker = false,
                        showTimePicker = true,
                        pickedReminderDateMillis = action.utcDateMillis
                    )
                }
            NoteDetailAction.OnDatePickerDismissed ->
                updateState { copy(showDatePicker = false) }
            is NoteDetailAction.OnReminderTimeSelected ->
                confirmReminderTime(action.hour, action.minute)
            NoteDetailAction.OnTimePickerDismissed ->
                updateState { copy(showTimePicker = false, pickedReminderDateMillis = null) }
            NoteDetailAction.OnClearReminderClicked ->
                updateState { copy(reminderAt = null, reminderFiredAt = null) }
        }
    }

    private fun confirmReminderTime(hour: Int, minute: Int) {
        val pickedDateMillis = state.value.pickedReminderDateMillis
        updateState { copy(showTimePicker = false, pickedReminderDateMillis = null) }
        if (pickedDateMillis == null) return

        // DatePickerState.selectedDateMillis (staged as pickedReminderDateMillis) is UTC midnight
        // of the chosen day, not device-local midnight. Naively seeding a local Calendar with it
        // and overwriting hour/minute can land on the wrong day for timezones behind UTC (UTC
        // midnight Jan 15 is Jan 14 evening in US timezones). Read the day out of a UTC calendar,
        // then build the real instant in a local calendar using the locally-picked hour/minute.
        val utcDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            timeInMillis = pickedDateMillis
        }
        val localInstant = Calendar.getInstance().apply {
            set(
                utcDate.get(Calendar.YEAR),
                utcDate.get(Calendar.MONTH),
                utcDate.get(Calendar.DAY_OF_MONTH),
                hour,
                minute,
                0
            )
            set(Calendar.MILLISECOND, 0)
        }
        val epochMillis = localInstant.timeInMillis

        if (epochMillis <= System.currentTimeMillis()) {
            emitEvent(NoteDetailEvent.ShowSnackbar(UiText.StringResource(R.string.error_reminder_in_past)))
            return
        }
        // A new reminderAt always resets reminderFiredAt — otherwise re-scheduling an
        // already-fired reminder to a new time would leave it permanently "fired" and
        // it would never actually push.
        updateState { copy(reminderAt = epochMillis, reminderFiredAt = null) }
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
            deleteNoteUseCase(
                noteId = state.value.noteId,
                noteCreatedAt = state.value.createdAt,
                hadImage = state.value.imageUrl != null,
            )
                .onSuccess { emitEvent(NoteDetailEvent.NavigateBack) }
                .onError { error ->
                    emitEvent(NoteDetailEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun pinNote() {
        viewModelScope.launch {
            updateNoteUseCase(buildNoteFromState(pinned = !state.value.pinned))
                .onError { error ->
                    emitEvent(NoteDetailEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    // Builds a Note from current state with overrides
    private fun buildNoteFromState(
        pinned: Boolean = state.value.pinned,
        title: String = state.value.title,
        content: String = state.value.content
    ): Note = Note(
        id = state.value.noteId,
        userId = state.value.userId,
        title = title.trim(),
        content = content.trim(),
        createdAt = state.value.createdAt,
        updatedAt = System.currentTimeMillis(),
        pinned = pinned,
        synced = false,
        imageUrl = state.value.imageUrl,
        reminderAt = state.value.reminderAt,
        reminderFiredAt = state.value.reminderFiredAt
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
                            // Log Image Added
                            analyticsTracker.logEvent(
                                AnalyticsEvent.NoteImageAdded(AnalyticsEvent.ImageSource.EDIT)
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