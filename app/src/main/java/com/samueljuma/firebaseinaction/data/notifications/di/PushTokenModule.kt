package com.samueljuma.firebaseinaction.data.notifications.di

import com.google.firebase.messaging.FirebaseMessaging
import com.samueljuma.firebaseinaction.data.notifications.AndroidNotificationDisplayer
import com.samueljuma.firebaseinaction.data.notifications.PushTokenRepositoryImpl
import com.samueljuma.firebaseinaction.domain.notifications.NotificationDisplayer
import com.samueljuma.firebaseinaction.domain.notifications.PushTokenRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module


val pushTokenModule = module {
    single { FirebaseMessaging.getInstance() }
    singleOf(::PushTokenRepositoryImpl).bind<PushTokenRepository>()
    singleOf(::AndroidNotificationDisplayer).bind<NotificationDisplayer>()
}