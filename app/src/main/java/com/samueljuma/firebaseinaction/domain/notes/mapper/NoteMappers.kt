package com.samueljuma.firebaseinaction.domain.notes.mapper

import com.samueljuma.firebaseinaction.data.notes.local.NoteEntity
import com.samueljuma.firebaseinaction.data.notes.remote.NoteDto
import com.samueljuma.firebaseinaction.domain.notes.model.Note

// Entity → Domain
fun NoteEntity.toNote() = Note(
    id = id,
    userId = userId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinned = pinned,
    synced = synced,
    imageUrl = imageUrl,
    reminderAt = reminderAt,
    reminderFiredAt = reminderFiredAt
)

// Domain → Entity
fun Note.toEntity() = NoteEntity(
    id = id,
    userId = userId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinned = pinned,
    synced = synced,
    imageUrl = imageUrl,
    reminderAt = reminderAt,
    reminderFiredAt = reminderFiredAt
)

// Dto → Entity (from Firestore — always synced)
fun NoteDto.toEntity() = NoteEntity(
    id = id,
    userId = userId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinned = pinned,
    synced = true,
    imageUrl = imageUrl,
    reminderAt = reminderAt,
    reminderFiredAt = reminderFiredAt
)

// Entity → Dto (going to Firestore)
fun NoteEntity.toDto() = NoteDto(
    id = id,
    userId = userId,
    title = title,
    content = content,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pinned = pinned,
    imageUrl = imageUrl,
    reminderAt = reminderAt,
    reminderFiredAt = reminderFiredAt
)
