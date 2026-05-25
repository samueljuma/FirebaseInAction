package com.samueljuma.firebaseinaction.presentation.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.domain.auth.SessionStorage
import kotlinx.coroutines.launch


class MainViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {

    var state by mutableStateOf(MainState())
        private set

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            // SessionStorage.get() is a local read — fast, no network
            val session = sessionStorage.get()
            state = state.copy(
                isCheckingAuth = false,
                isLoggedIn = session != null
            )
        }
    }
}