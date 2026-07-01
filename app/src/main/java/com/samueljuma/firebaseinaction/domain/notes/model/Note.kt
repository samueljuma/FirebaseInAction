package com.samueljuma.firebaseinaction.domain.notes.model

data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val pinned: Boolean,
    val synced: Boolean,
    val imageUrl: String? = null,
    // When set, a reminder push is due at this time. reminderFiredAt is null while pending —
    // set once the reminder function has delivered it, so it never fires twice.
    val reminderAt: Long? = null,
    val reminderFiredAt: Long? = null
)