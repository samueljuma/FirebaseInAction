package com.samueljuma.firebaseinaction.presentation.ui.createnotes

data class CreateNoteState(
    val title: String = "",
    val content: String = "",
    val isSaving: Boolean = false,
    val imageUrl: String? = null,
    val isUploadingImage: Boolean = false,
    val uploadProgress: Int? = null
)