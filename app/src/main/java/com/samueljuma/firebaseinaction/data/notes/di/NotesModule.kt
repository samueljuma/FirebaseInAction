package com.samueljuma.firebaseinaction.data.notes.di

import androidx.work.WorkManager
import com.google.firebase.firestore.FirebaseFirestore
import com.samueljuma.firebaseinaction.data.notes.NoteRepositoryImpl
import com.samueljuma.firebaseinaction.data.notes.NoteSyncWorker
import com.samueljuma.firebaseinaction.data.util.FirestoreIdGenerator
import com.samueljuma.firebaseinaction.domain.notes.CreateNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.DeleteNoteUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNoteByIdUseCase
import com.samueljuma.firebaseinaction.domain.notes.GetNotesUseCase
import com.samueljuma.firebaseinaction.domain.notes.NoteRepository
import com.samueljuma.firebaseinaction.domain.notes.StartRemoteSyncUseCase
import com.samueljuma.firebaseinaction.domain.notes.UpdateNoteUseCase
import com.samueljuma.firebaseinaction.domain.util.IdGenerator
import com.samueljuma.firebaseinaction.presentation.ui.createnotes.CreateNoteViewModel
import com.samueljuma.firebaseinaction.presentation.ui.home.HomeViewModel
import com.samueljuma.firebaseinaction.presentation.ui.notedetails.NoteDetailViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notesModule = module {
    single { FirebaseFirestore.getInstance() }
    single { WorkManager.getInstance(androidContext()) }

    singleOf(::NoteRepositoryImpl).bind<NoteRepository>()
    singleOf(::FirestoreIdGenerator).bind<IdGenerator>()

    factoryOf(::GetNotesUseCase)
    factoryOf(::GetNoteByIdUseCase)
    factoryOf(::CreateNoteUseCase)
    factoryOf(::UpdateNoteUseCase)
    factoryOf(::DeleteNoteUseCase)
    factoryOf(::StartRemoteSyncUseCase)

    viewModelOf(::HomeViewModel)
    viewModelOf(::CreateNoteViewModel)
    viewModelOf(::NoteDetailViewModel)

    workerOf(::NoteSyncWorker)
}
