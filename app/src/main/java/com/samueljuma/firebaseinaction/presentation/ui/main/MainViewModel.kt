package com.samueljuma.firebaseinaction.presentation.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.domain.auth.usecases.ClearSessionUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserUseCase
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetSessionUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.ReloadCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val getSessionUseCase: GetSessionUseCase,
    private val clearSessionUseCase: ClearSessionUseCase,
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val reloadCurrentUserUseCase: ReloadCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // Phase 1: Local check — instant, no network
            val localSession = getSessionUseCase()
            val currentUser = getCurrentUserSyncUseCase()

            state = when {
                localSession != null && currentUser != null ->
                    state.copy(
                        isCheckingAuth = false,
                        isLoggedIn = true,
                        isEmailVerified = currentUser.isEmailVerified
                    )
                localSession != null && currentUser == null -> {
                    clearSessionUseCase()
                    state.copy(isCheckingAuth = false, isLoggedIn = false)
                }
                else ->
                    state.copy(isCheckingAuth = false, isLoggedIn = false)
            }

            // Phase 2: Start observers — non-blocking
            observeTokenExpiry()

            // Phase 3: Background server verification — only for fully established sessions.
            // Skipped for unverified users: EmailVerificationScreen's checkVerification()
            // already calls reloadCurrentUser(), so they get server validation there.
            // Running reload() concurrently with sendEmailVerification() in init causes a
            // race where Firebase's token re-validation transiently clears currentUser.
            if (state.isLoggedIn && state.isEmailVerified) {
                verifySessionWithServer()
            }
        }
    }

    private fun verifySessionWithServer() {
        // Intentionally a separate coroutine — fire and forget
        // Does not suspend checkAuthState
        viewModelScope.launch {
            reloadCurrentUserUseCase()
                .onError { error ->
                    when (error) {
                        DataError.Auth.USER_NOT_FOUND -> {
                            Timber.tag(TAG).w("Account deleted — forcing sign out")
                            clearSessionUseCase()
                            state = state.copy(
                                isLoggedIn = false,
                                sessionExpired = true
                            )
                        }
                        DataError.Auth.NETWORK_ERROR -> {
                            // Offline — trust local session silently
                            Timber.tag(TAG).d("Offline — skipping server verification")
                        }
                        else -> Unit
                    }
                }
        }
    }

    private fun observeTokenExpiry() {
        viewModelScope.launch {
            Timber.tag(TAG).d("Starting token expiry observation")
            getCurrentUserUseCase().collect { user ->
                Timber.tag(TAG).d("Auth state emission — user: ${user?.uid ?: "null"}")
                if (user == null && state.isLoggedIn) {
                    // Check whether a session still exists in DataStore to distinguish:
                    //   - Explicit sign-out: signOut() clears DataStore before calling
                    //     firebaseAuth.signOut(), so the session is already gone here.
                    //     Just update isLoggedIn — no dialog.
                    //   - Unexpected expiry / account deleted: the session is still in
                    //     DataStore because nobody cleared it. Clear it and show the dialog.
                    val sessionExists = getSessionUseCase() != null
                    if (sessionExists) {
                        Timber.tag(TAG).w("Session expired unexpectedly — clearing")
                        clearSessionUseCase()
                        state = state.copy(sessionExpired = true)
                    } else {
                        Timber.tag(TAG).d("Explicit sign-out detected — no dialog")
                        state = state.copy(isLoggedIn = false)
                    }
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