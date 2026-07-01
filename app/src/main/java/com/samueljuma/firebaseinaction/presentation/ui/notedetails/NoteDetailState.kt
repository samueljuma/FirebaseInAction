package com.samueljuma.firebaseinaction.presentation.ui.notedetails

data class NoteDetailState(
    val noteId: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val pinned: Boolean = false,
    val synced: Boolean = false,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditing: Boolean = false,
    val createdAt: Long = 0L,
    val lastUpdated: Long = 0L,
    val imageUrl: String? = null,
    val uploadProgress: Int? = null,  // null = not uploading, 0-100 = progress
    val isUploadingImage: Boolean = false,
    val showCancelUploadDialog: Boolean = false,
    val reminderAt: Long? = null,
    // Not rendered directly, but must round-trip on every save so an unrelated edit
    // (e.g. pinning) can't accidentally clear a reminder's already-fired status and
    // cause it to re-fire. Only reset to null when the user picks a *new* reminderAt.
    val reminderFiredAt: Long? = null,
    // Reminder picker flow — held here (not composable-local `remember`) so it survives
    // activity recreation (e.g. rotation); ViewModel state outlives the Composition.
    val showDatePicker: Boolean = false,
    val showTimePicker: Boolean = false,
    // The UTC-midnight day picked in step 1, staged until a time is confirmed in step 2.
    val pickedReminderDateMillis: Long? = null
)