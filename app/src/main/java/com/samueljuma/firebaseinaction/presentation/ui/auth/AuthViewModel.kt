package com.samueljuma.firebaseinaction.presentation.ui.auth

import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.data.auth.GoogleAuthHandler
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.GoogleSignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignUpUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val googleAuthHandler: GoogleAuthHandler
) : MviViewModel<AuthState, AuthAction, AuthEvent>(
    initialState = AuthState()
) {

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnEmailChanged -> updateState { copy(email = action.email) }
            is AuthAction.OnPasswordChanged -> updateState { copy(password = action.password) }
            AuthAction.OnTogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            AuthAction.OnSignInClicked -> signIn()
            AuthAction.OnSignUpClicked -> signUp()
            is AuthAction.OnGoogleSignInClicked -> signInWithGoogle()
            AuthAction.OnGoToSignUp -> Unit
            AuthAction.OnGoToLogin -> Unit
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            googleAuthHandler.getGoogleIdToken()
                .onSuccess { idToken ->
                    googleSignInUseCase(idToken)
                        .onSuccess {
                            emitEvent(AuthEvent.Login.LoginSuccess)
                            emitEvent(AuthEvent.SignUp.SignUpSuccess)
                        }
                        .onError { error ->
                            emitEvent(AuthEvent.Login.ShowSnackbar(error.toUiText()))
                            emitEvent(AuthEvent.SignUp.ShowSnackbar(error.toUiText()))
                        }
                }
                .onError { error ->
                    // Silently ignore cancellation — user chose to dismiss
                    if (error != DataError.Auth.CANCELLED) {
                        emitEvent(AuthEvent.Login.ShowSnackbar(error.toUiText()))
                    }
                }

            updateState { copy(isLoading = false) }
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            signInUseCase(
                email = state.value.email,
                password = state.value.password
            )
                .onSuccess { emitEvent(AuthEvent.Login.LoginSuccess) }
                .onError { error -> emitEvent(AuthEvent.Login.ShowSnackbar(error.toUiText())) }

            updateState { copy(isLoading = false) }
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            signUpUseCase(
                email = state.value.email,
                password = state.value.password
            )
                .onSuccess { emitEvent(AuthEvent.SignUp.SignUpSuccess) }
                .onError { error -> emitEvent(AuthEvent.SignUp.ShowSnackbar(error.toUiText())) }

            updateState { copy(isLoading = false) }
        }
    }

}