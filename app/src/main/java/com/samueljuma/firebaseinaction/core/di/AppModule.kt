package com.samueljuma.firebaseinaction.core.di

import androidx.room.Room
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.samueljuma.firebaseinaction.BuildConfig
import com.samueljuma.firebaseinaction.data.auth.AuthRepositoryImpl
import com.samueljuma.firebaseinaction.data.auth.DataStoreSessionStorage
import com.samueljuma.firebaseinaction.data.auth.GoogleAuthHandler
import com.samueljuma.firebaseinaction.data.notes.NoteRepositoryImpl
import com.samueljuma.firebaseinaction.data.notes.NoteSyncWorker
import com.samueljuma.firebaseinaction.data.notes.local.AppDatabase
import com.samueljuma.firebaseinaction.data.util.FirestoreIdGenerator
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
import com.samueljuma.firebaseinaction.domain.notes.CreateNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.DeleteNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNotesUseCase
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.notes.StartRemoteSyncUseCase
import com.samueljuma.firebaseinaction.domain.notes.UpdateNoteUseCase
import com.samueljuma.firebaseinaction.domain.util.IdGenerator
import com.samueljuma.firebaseinaction.presentation.ui.auth.AuthViewModel
import com.samueljuma.firebaseinaction.presentation.ui.createnotes.CreateNoteViewModel
import com.samueljuma.firebaseinaction.presentation.ui.home.HomeViewModel
import com.samueljuma.firebaseinaction.presentation.ui.main.MainViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
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

    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "firebaseinaction.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true) // dev only
            .build()
    }

    single { get<AppDatabase>().noteDao() }

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
    viewModelOf(::MainViewModel)

    // Firestore
    single { FirebaseFirestore.getInstance() }
    // WorkManager
    single { WorkManager.getInstance(androidContext()) }

    // Notes Repository
    singleOf(::NoteRepositoryImpl).bind<NoteRepository>()

    singleOf(::FirestoreIdGenerator).bind<IdGenerator>()
    // Notes UseCases
    factoryOf(::GetNotesUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::StartRemoteSyncUseCase)
    viewModelOf(::HomeViewModel)
    viewModelOf(::CreateNoteViewModel)
    workerOf(::NoteSyncWorker)
}