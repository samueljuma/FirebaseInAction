package com.samueljuma.firebaseinaction.core.di

import com.google.firebase.auth.FirebaseAuth
import com.samueljuma.firebaseinaction.BuildConfig
import com.samueljuma.firebaseinaction.data.auth.AuthRepositoryImpl
import com.samueljuma.firebaseinaction.data.auth.GoogleAuthHandler
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.GetCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.GoogleSignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignUpUseCase
import com.samueljuma.firebaseinaction.presentation.ui.auth.AuthViewModel
import com.samueljuma.firebaseinaction.presentation.ui.home.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
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
    viewModelOf(::AuthViewModel)
    viewModelOf(::HomeViewModel)
}