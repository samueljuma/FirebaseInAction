package com.samueljuma.firebaseinaction.core.di

import com.samueljuma.firebaseinaction.data.auth.di.authModule
import com.samueljuma.firebaseinaction.data.config.di.remoteConfigModule
import com.samueljuma.firebaseinaction.data.observability.observabilityModule
import com.samueljuma.firebaseinaction.data.notes.di.notesModule
import com.samueljuma.firebaseinaction.data.notes.local.di.dataBaseModule
import com.samueljuma.firebaseinaction.data.notifications.di.notificationsModule
import com.samueljuma.firebaseinaction.data.notifications.di.pushTokenModule
import com.samueljuma.firebaseinaction.data.storage.storageModule
import com.samueljuma.firebaseinaction.data.sync.di.syncModule

val appModules = listOf(
    coreModule,
    remoteConfigModule,
    authModule,
    notesModule,
    dataBaseModule,
    storageModule,
    syncModule,
    observabilityModule,
    pushTokenModule,
    notificationsModule
)
