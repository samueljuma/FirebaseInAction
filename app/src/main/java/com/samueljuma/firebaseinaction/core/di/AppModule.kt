package com.samueljuma.firebaseinaction.core.di

import com.samueljuma.firebaseinaction.data.auth.di.authModule
import com.samueljuma.firebaseinaction.data.notes.di.notesModule
import com.samueljuma.firebaseinaction.data.notes.local.di.dataBaseModule
import com.samueljuma.firebaseinaction.data.storage.storageModule

val appModules = listOf(
    authModule,
    notesModule,
    dataBaseModule,
    storageModule
)