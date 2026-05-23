package com.samueljuma.firebaseinaction

import android.app.Application
import com.samueljuma.firebaseinaction.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class FirebaseInActionApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@FirebaseInActionApp)
            modules(appModule)
        }
    }
}