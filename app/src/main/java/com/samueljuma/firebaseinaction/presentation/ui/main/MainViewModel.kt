package com.samueljuma.firebaseinaction.presentation.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import com.samueljuma.firebaseinaction.domain.auth.usecases.ClearSessionUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetSessionUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val getSessionUseCase: GetSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            val localSession = getSessionUseCase()
            val currentUser = getCurrentUserSyncUseCase()

            state = when {
                localSession != null && currentUser != null ->
                    state.copy(isCheckingAuth = false, isLoggedIn = true)
                localSession != null && currentUser == null -> {
                    clearSessionUseCase()
                    state.copy(isCheckingAuth = false, isLoggedIn = false)
                }
                else ->
                    state.copy(isCheckingAuth = false, isLoggedIn = false)
            }

            observeTokenExpiry()
        }
    }

    private fun observeTokenExpiry() {
        viewModelScope.launch {
            Timber.tag(TAG).d("Starting token expiry observation")
            getCurrentUserUseCase().collect { user ->
                Timber.tag(TAG).d("Auth state emission — user: ${user?.uid ?: "null"}")
                if (user == null && state.isLoggedIn) {
                    Timber.tag(TAG).w("Session expired — clearing")
                    clearSessionUseCase()
                    state = state.copy(sessionExpired = true)
                }
            }
        }
    }

    fun onSessionExpiredDismissed() {
        viewModelScope.launch {
            signOutUseCase()
            state = state.copy(sessionExpired = false, isLoggedIn = false)
        }
    }

    fun onReLoginClicked() {
        viewModelScope.launch {
            signOutUseCase()
            state = state.copy(sessionExpired = false, isLoggedIn = false)
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}