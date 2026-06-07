package com.samueljuma.firebaseinaction.domain.notes.model

data class Note(
    val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val isSynced: Boolean,
    val imageUrl: String? = null
)