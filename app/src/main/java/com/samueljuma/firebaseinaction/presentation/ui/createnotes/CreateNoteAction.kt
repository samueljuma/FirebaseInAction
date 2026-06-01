package com.samueljuma.firebaseinaction.presentation.ui.createnotes

sealed interface CreateNoteAction {
    data class OnTitleChanged(val title: String) : CreateNoteAction
    data class OnContentChanged(val content: String) : CreateNoteAction
    data object OnSaveClicked : CreateNoteAction
    data object OnBackClicked : CreateNoteAction
}