package com.samueljuma.firebaseinaction.data.notes.remote

data class NoteDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val content: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val isPinned: Boolean = false
)