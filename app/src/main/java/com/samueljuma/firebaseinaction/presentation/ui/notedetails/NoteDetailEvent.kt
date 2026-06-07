package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import com.samueljuma.firebaseinaction.core.utils.UiText

sealed interface NoteDetailEvent {
    data object NavigateBack : NoteDetailEvent
    data class ShowSnackbar(val message: UiText) : NoteDetailEvent
}