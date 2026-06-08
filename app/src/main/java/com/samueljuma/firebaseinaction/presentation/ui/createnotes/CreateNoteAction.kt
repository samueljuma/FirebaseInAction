package com.samueljuma.firebaseinaction.presentation.ui.createnotes

import android.net.Uri

sealed interface CreateNoteAction {
    data class OnTitleChanged(val title: String) : CreateNoteAction
    data class OnContentChanged(val content: String) : CreateNoteAction
    data object OnSaveClicked : CreateNoteAction
    data object OnBackClicked : CreateNoteAction
    data object OnImageClicked : CreateNoteAction
    data class OnImageSelected(val uri: Uri) : CreateNoteAction
    data object OnRemoveImageClicked : CreateNoteAction
    data object OnCancelUploadConfirmed : CreateNoteAction
    data object OnCancelUploadDismissed : CreateNoteAction
}