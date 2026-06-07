package com.samueljuma.firebaseinaction.presentation.ui.notedetails

sealed interface NoteDetailAction {
    data object OnBackClicked : NoteDetailAction
    data object OnEditClicked : NoteDetailAction
    data object OnSaveClicked : NoteDetailAction
    data object OnDeleteClicked : NoteDetailAction
    data object OnPinClicked : NoteDetailAction
    data class OnTitleChanged(val title: String) : NoteDetailAction
    data class OnContentChanged(val content: String) : NoteDetailAction
}