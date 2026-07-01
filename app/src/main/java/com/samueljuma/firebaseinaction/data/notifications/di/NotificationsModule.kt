package com.samueljuma.firebaseinaction.data.notifications.di

import com.samueljuma.firebaseinaction.data.notifications.NotificationRepositoryImpl
import com.samueljuma.firebaseinaction.domain.notifications.NotificationRepository
import com.samueljuma.firebaseinaction.domain.notifications.usecases.DeleteNotificationUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.GetNotificationsUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.GetUnreadCountUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.MarkNotificationReadUseCase
import com.samueljuma.firebaseinaction.domain.notifications.usecases.StartNotificationSyncUseCase
import com.samueljuma.firebaseinaction.presentation.ui.notifications.NotificationsViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notificationsModule = module {
    singleOf(::NotificationRepositoryImpl).bind<NotificationRepository>()

    factoryOf(::GetNotificationsUseCase)
    factoryOf(::GetUnreadCountUseCase)
    factoryOf(::MarkNotificationReadUseCase)
    factoryOf(::DeleteNotificationUseCase)
    factoryOf(::StartNotificationSyncUseCase)

    viewModelOf(::NotificationsViewModel)
}