package com.samueljuma.firebaseinaction.presentation.ui.notedetails

data class NoteDetailState(
    val noteId: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val isPinned: Boolean = false,
    val isSynced: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val createdAt: Long = 0L,
    val lastUpdated: Long = 0L
)