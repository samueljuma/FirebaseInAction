package com.samueljuma.firebaseinaction.core.lifecycle

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import timber.log.Timber

object AppForegroundTracker : DefaultLifecycleObserver {

    @Volatile
    var isAppInForeground: Boolean = false
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        isAppInForeground = true
        Timber.tag("AppForeground").d("App entered foreground")
    }

    override fun onStop(owner: LifecycleOwner) {
        isAppInForeground = false
        Timber.tag("AppForeground").d("App entered background")
    }

}