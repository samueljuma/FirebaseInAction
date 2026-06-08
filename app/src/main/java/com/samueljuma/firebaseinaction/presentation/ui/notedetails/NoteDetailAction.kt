package com.samueljuma.firebaseinaction.presentation.ui.notedetails

import android.net.Uri

sealed interface NoteDetailAction {
    data object OnBackClicked : NoteDetailAction
    data object OnEditClicked : NoteDetailAction
    data object OnSaveClicked : NoteDetailAction
    data object OnDeleteClicked : NoteDetailAction
    data object OnPinClicked : NoteDetailAction
    data class OnTitleChanged(val title: String) : NoteDetailAction
    data class OnContentChanged(val content: String) : NoteDetailAction
    data object OnImageClicked : NoteDetailAction          // open picker
    data class OnImageSelected(val uri: Uri) : NoteDetailAction  // picker result
    data object OnRemoveImageClicked : NoteDetailAction
    data object OnCancelUploadConfirmed : NoteDetailAction
    data object OnCancelUploadDismissed : NoteDetailAction
}