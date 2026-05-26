package com.samueljuma.firebaseinaction.core.di

import com.google.firebase.auth.FirebaseAuth
import com.samueljuma.firebaseinaction.BuildConfig
import com.samueljuma.firebaseinaction.data.auth.AuthRepositoryImpl
import com.samueljuma.firebaseinaction.data.auth.DataStoreSessionStorage
import com.samueljuma.firebaseinaction.data.auth.GoogleAuthHandler
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.auth.usecases.ClearSessionUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.ReloadCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetSessionUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GoogleSignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignUpUseCase
import com.samueljuma.firebaseinaction.presentation.ui.auth.AuthViewModel
import com.samueljuma.firebaseinaction.presentation.ui.home.HomeViewModel
import com.samueljuma.firebaseinaction.presentation.ui.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
    single {
        GoogleAuthHandler(
            context = androidContext(),
            webClientId = BuildConfig.WEB_CLIENT_ID
        )
    }

    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
    factoryOf(::SignInUseCase)
    factoryOf(::SignUpUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::GoogleSignInUseCase)
    factoryOf(::GetCurrentUserUseCase)
    factoryOf(::GetCurrentUserSyncUseCase)
    factoryOf(::GetSessionUseCase)
    factoryOf(::ClearSessionUseCase)
    factoryOf(::ReloadCurrentUserUseCase)
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::MainViewModel)
}