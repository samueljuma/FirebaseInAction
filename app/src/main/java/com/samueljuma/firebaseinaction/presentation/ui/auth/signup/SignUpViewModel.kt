package com.samueljuma.firebaseinaction.presentation.ui.auth.signup

import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.DataError
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.data.auth.GoogleAuthHandler
import com.samueljuma.firebaseinaction.domain.auth.usecases.GoogleSignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignUpUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch

class SignUpViewModel(
    private val signUpUseCase: SignUpUseCase,
    private val googleSignInUseCase: GoogleSignInUseCase,
    private val googleAuthHandler: GoogleAuthHandler
) : MviViewModel<SignUpState, SignUpAction, SignUpEvent>(SignUpState()) {

    override fun onAction(action: SignUpAction) {
        when (action) {
            is SignUpAction.OnEmailChanged -> updateState { copy(email = action.email) }
            is SignUpAction.OnPasswordChanged -> updateState { copy(password = action.password) }
            SignUpAction.OnTogglePasswordVisibility -> updateState { copy(isPasswordVisible = !isPasswordVisible) }
            SignUpAction.OnSignUpClicked -> signUp()
            SignUpAction.OnGoogleSignInClicked -> signInWithGoogle()
            SignUpAction.OnGoToLogin -> Unit
        }
    }

    private fun signUp() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            signUpUseCase(email = state.value.email, password = state.value.password)
                .onSuccess { emitEvent(SignUpEvent.SignUpSuccess) }
                .onError { error -> emitEvent(SignUpEvent.ShowSnackbar(error.toUiText())) }
            updateState { copy(isLoading = false) }
        }
    }

    private fun signInWithGoogle() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            googleAuthHandler.getGoogleIdToken()
                .onSuccess { idToken ->
                    googleSignInUseCase(idToken)
                        .onSuccess { emitEvent(SignUpEvent.SignUpSuccess) }
                        .onError { error -> emitEvent(SignUpEvent.ShowSnackbar(error.toUiText())) }
                }
                .onError { error ->
                    when (error) {
                        DataError.Auth.CANCELLED -> Unit
                        DataError.Auth.NO_GOOGLE_ACCOUNT -> emitEvent(SignUpEvent.OpenGoogleAccountSettings)
                        else -> emitEvent(SignUpEvent.ShowSnackbar(error.toUiText()))
                    }
                }
            updateState { copy(isLoading = false) }
        }
    }
}
