package com.samueljuma.firebaseinaction.data.notes.local.di

import androidx.room.Room
import com.samueljuma.firebaseinaction.data.notes.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataBaseModule = module {
    single { get<AppDatabase>().noteDao() }
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = "firebaseinaction.db"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()
    }
//    single {
//        Room.databaseBuilder(
//            context = androidContext(),
//            klass = AppDatabase::class.java,
//            name = "firebaseinaction.db"
//        )
//            .fallbackToDestructiveMigration(dropAllTables = true) // dev only
//            .build()
//    }
}