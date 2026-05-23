package com.samueljuma.firebaseinaction.core.di

import com.google.firebase.auth.FirebaseAuth
import com.samueljuma.firebaseinaction.data.auth.AuthRepositoryImpl
import com.samueljuma.firebaseinaction.domain.auth.AuthRepository
import com.samueljuma.firebaseinaction.domain.auth.GetCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignUpUseCase
import com.samueljuma.firebaseinaction.presentation.ui.auth.AuthViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { FirebaseAuth.getInstance() }
    singleOf(::AuthRepositoryImpl).bind<AuthRepository>()
    factoryOf(::SignInUseCase)
    factoryOf(::SignUpUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::GetCurrentUserUseCase)
    viewModelOf(::AuthViewModel)
}