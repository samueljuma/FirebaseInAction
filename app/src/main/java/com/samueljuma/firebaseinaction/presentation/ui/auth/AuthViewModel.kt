package com.samueljuma.firebaseinaction.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.domain.auth.GetCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignInUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignOutUseCase
import com.samueljuma.firebaseinaction.domain.auth.SignUpUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch

class AuthViewModel(
    private val signInUseCase: SignInUseCase,
    private val signUpUseCase: SignUpUseCase,
) : MviViewModel<AuthState, AuthAction, AuthEvent>(
    initialState = AuthState()
) {

    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnEmailChanged ->
                updateState { copy(email = action.email) }

            is AuthAction.OnPasswordChanged ->
                updateState { copy(password = action.password) }

            AuthAction.OnTogglePasswordVisibility ->
                updateState { copy(isPasswordVisible = !isPasswordVisible) }

            AuthAction.OnSignInClicked -> signIn()
            AuthAction.OnSignUpClicked -> signUp()
            AuthAction.OnGoToSignUp -> Unit
            AuthAction.OnGoToLogin -> Unit
        }
    }

    private fun signIn() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }
            signInUseCase(
                email = state.value.email,
                password = state.value.password
            )
                .onSuccess { emitEvent(AuthEvent.Login.NavigateToHome) }
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
                .onSuccess { emitEvent(AuthEvent.SignUp.NavigateToHome) }
                .onError { error -> emitEvent(AuthEvent.SignUp.ShowSnackbar(error.toUiText())) }

            updateState { copy(isLoading = false) }
        }
    }

}