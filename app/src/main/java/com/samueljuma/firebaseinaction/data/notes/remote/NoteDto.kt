package com.samueljuma.firebaseinaction.data.notes.remote

data class NoteDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    // Firebase JavaBeans convention strips "is" from isXxx() getters, storing "pinned" not "isPinned".
    // Using "pinned" aligns write and read field names so round-trips work correctly.
    val pinned: Boolean = false,
    val imageUrl: String? = null,
    val reminderAt: Long? = null,
    val reminderFiredAt: Long? = null
)