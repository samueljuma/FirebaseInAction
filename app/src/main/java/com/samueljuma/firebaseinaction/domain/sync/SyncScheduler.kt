package com.samueljuma.firebaseinaction.domain.sync

interface SyncScheduler {
    fun scheduleNotesSync()
    fun scheduleImageUpload(noteId: String, imageUri: String)
    fun cancelImageUpload(noteId: String)
    fun cancelAllSyncs()
}
