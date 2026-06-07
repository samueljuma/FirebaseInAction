package com.samueljuma.firebaseinaction.presentation.ui.auth.signin

import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.data.auth.GoogleAuthHandler
import com.samueljuma.firebaseinaction.domain.auth.usecases.GoogleSignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignInUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch

class SignInViewModel(
    private val signInUseCase: SignInUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val googleAuthHandler: GoogleAuthHandler
) : MviViewModel<SignInState, SignInAction, SignInEvent>(SignInState()) {

    override fun onAction(action: SignInAction) {
        when (action) {
            is SignInAction.OnEmailChanged -> updateState { copy(email = action.email) }
            is SignInAction.OnPasswordChanged -> updateState { copy(password = action.password) }
            SignInAction.OnTogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            SignInAction.OnSignInClicked -> signIn()
            SignInAction.OnGoogleSignInClicked -> signInWithGoogle()
            SignInAction.OnGoToSignUp -> Unit
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            signInUseCase(email = state.value.email, password = state.value.password)
                .onSuccess { emitEvent(SignInEvent.SignInSuccess) }
                .onError { error -> emitEvent(SignInEvent.ShowSnackbar(error.toUiText())) }
            updateState { copy(isLoading = false) }
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            googleAuthHandler.getGoogleIdToken()
                .onSuccess { idToken ->
                    googleSignInUseCase(idToken)
                        .onSuccess { emitEvent(SignInEvent.SignInSuccess) }
                        .onError { error -> emitEvent(SignInEvent.ShowSnackbar(error.toUiText())) }
                }
                .onError { error ->
                    when (error) {
                        DataError.Auth.CANCELLED -> Unit
                        DataError.Auth.NO_GOOGLE_ACCOUNT -> emitEvent(SignInEvent.OpenGoogleAccountSettings)
                        else -> emitEvent(SignInEvent.ShowSnackbar(error.toUiText()))
                    }
                }
            updateState { copy(isLoading = false) }
        }
    }
}
