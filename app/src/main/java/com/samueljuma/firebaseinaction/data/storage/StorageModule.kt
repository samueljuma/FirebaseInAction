package com.samueljuma.firebaseinaction.data.storage

import com.google.firebase.storage.FirebaseStorage
import com.samueljuma.firebaseinaction.data.notes.workers.ImageUploadWorker
import com.samueljuma.firebaseinaction.domain.storage.StorageRepository
import com.samueljuma.firebaseinaction.domain.storage.usecases.DeleteImageOnlyUseCase
import com.samueljuma.firebaseinaction.domain.storage.usecases.DeleteNoteImageUseCase
import com.samueljuma.firebaseinaction.domain.storage.usecases.UploadImageOnlyUseCase
import com.samueljuma.firebaseinaction.domain.storage.usecases.UploadNoteImageUseCase
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val storageModule = module {
    single { FirebaseStorage.getInstance() }
    singleOf(::StorageRepositoryImpl).bind<StorageRepository>()
    singleOf(::UploadNoteImageUseCase)
    singleOf(::DeleteNoteImageUseCase)
    factoryOf(::UploadImageOnlyUseCase)
    factoryOf(::DeleteImageOnlyUseCase)
    workerOf(::ImageUploadWorker)
}