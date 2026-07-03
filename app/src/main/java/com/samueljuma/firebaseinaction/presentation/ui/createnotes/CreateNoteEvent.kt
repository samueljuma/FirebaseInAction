package com.samueljuma.firebaseinaction.presentation.ui.createnotes

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface CreateNoteEvent {
    data object NavigateBack : CreateNoteEvent
    data class ShowSnackbar(val message: UiText) : CreateNoteEvent
    data object LaunchImagePicker : CreateNoteEvent
}