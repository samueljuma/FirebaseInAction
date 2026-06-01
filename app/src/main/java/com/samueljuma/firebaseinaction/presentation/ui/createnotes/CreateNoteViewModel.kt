package com.samueljuma.firebaseinaction.presentation.ui.createnotes

import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.utils.UiText
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetSessionUseCase
import com.samueljuma.firebaseinaction.domain.notes.CreateNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.model.Note
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch

class CreateNoteViewModel(
    private val createNoteUseCase: CreateNoteUseCase,
    private val getSessionUseCase: GetSessionUseCase
) : MviViewModel<CreateNoteState, CreateNoteAction, CreateNoteEvent>(
    CreateNoteState()
) {
    override fun onAction(action: CreateNoteAction) {
        when (action) {
            is CreateNoteAction.OnTitleChanged ->
                updateState { copy(title = action.title) }
            is CreateNoteAction.OnContentChanged ->
                updateState { copy(content = action.content) }
            CreateNoteAction.OnSaveClicked -> saveNote()
            CreateNoteAction.OnBackClicked ->
                emitEvent(CreateNoteEvent.NavigateBack)
        }
    }

    private fun saveNote() {
        viewModelScope.launch {
            val title = state.value.title.trim()
            val content = state.value.content.trim()

            if (title.isBlank()) {
                emitEvent(
                    CreateNoteEvent.ShowSnackbar(
                        UiText.StringResource(R.string.error_title_empty)
                    )
                )
                return@launch
            }

            updateState { copy(isSaving = true) }

            val userId = getSessionUseCase()?.uid ?: run {
                emitEvent(
                    CreateNoteEvent.ShowSnackbar(
                        UiText.StringResource(R.string.error_unknown)
                    )
                )
                updateState { copy(isSaving = false) }
                return@launch
            }

            // Generate ID locally — offline-first
            val noteId = FirebaseFirestore.getInstance()
                .collection("users/$userId/notes")
                .document()
                .id

            val note = Note(
                id = noteId,
                userId = userId,
                title = title,
                content = content,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isPinned = false,
                isSynced = false
            )

            createNoteUseCase(note)
                .onSuccess { emitEvent(CreateNoteEvent.NavigateBack) }
                .onError { error ->
                    emitEvent(CreateNoteEvent.ShowSnackbar(error.toUiText()))
                }

            updateState { copy(isSaving = false) }
        }
    }
}