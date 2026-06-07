package com.samueljuma.firebaseinaction.core.di

import com.samueljuma.firebaseinaction.data.auth.di.authModule
import com.samueljuma.firebaseinaction.data.notes.di.notesModule
import com.samueljuma.firebaseinaction.data.notes.local.di.dataBaseModule
import com.samueljuma.firebaseinaction.data.storage.storageModule
import com.samueljuma.firebaseinaction.data.sync.di.syncModule

val appModules = listOf(
    authModule,
    notesModule,
    dataBaseModule,
    storageModule,
    syncModule
)