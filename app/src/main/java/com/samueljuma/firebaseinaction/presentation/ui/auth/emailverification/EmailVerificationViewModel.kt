package com.samueljuma.firebaseinaction.presentation.ui.auth.emailverification

import androidx.lifecycle.viewModelScope
import com.samueljuma.firebaseinaction.R
import com.samueljuma.firebaseinaction.core.utils.UiText
import com.samueljuma.firebaseinaction.core.utils.onError
import com.samueljuma.firebaseinaction.core.utils.onSuccess
import com.samueljuma.firebaseinaction.core.utils.toUiText
import com.samueljuma.firebaseinaction.domain.auth.usecases.GetCurrentUserSyncUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.ReloadCurrentUserUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SendEmailVerificationUseCase
import com.samueljuma.firebaseinaction.domain.auth.usecases.SignOutUseCase
import com.samueljuma.firebaseinaction.presentation.ui.util.MviViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class EmailVerificationViewModel(
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val reloadCurrentUserUseCase: ReloadCurrentUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserSyncUseCase: GetCurrentUserSyncUseCase
): MviViewModel<EmailVerificationState, EmailVerificationAction, EmailVerificationEvent>(
    EmailVerificationState()
){

    init {
        // Load current user email for display
        val user = getCurrentUserSyncUseCase()
        updateState { copy(email = user?.email ?: "") }

        // Send verification email automatically on screen open
        sendVerificationEmail()
    }
    override fun onAction(action: EmailVerificationAction) {
        when(action){
            EmailVerificationAction.OnCheckVerificationClicked -> checkVerification()
            EmailVerificationAction.OnResendEmailClicked -> sendVerificationEmail()
            EmailVerificationAction.OnSignOutClicked -> signOut()
        }
    }

    private fun sendVerificationEmail(){
        viewModelScope.launch {
            sendEmailVerificationUseCase()
                .onSuccess {
                    emitEvent(EmailVerificationEvent.ShowSnackbar(UiText.DynamicString("Verification email sent")))
                    Timber.tag(TAG).d("Verification email sent")
                }
                .onError { error->
                    emitEvent(EmailVerificationEvent.ShowSnackbar(error.toUiText()))
                }
        }
    }

    private fun checkVerification(){
        viewModelScope.launch {
            updateState { copy(isCheckingVerification = true) }

            // Force reload — Firebase caches isEmailVerified locally
            // Without reload, it never updates even after user clicks the link
            reloadCurrentUserUseCase()
                .onSuccess {
                    val user = getCurrentUserSyncUseCase()
                    if (user?.isEmailVerified == true){
                        emitEvent(EmailVerificationEvent.NavigateToHome)
                    }else {
                        emitEvent(
                            EmailVerificationEvent.ShowSnackbar(
                                UiText.StringResource(R.string.error_email_not_verified)
                            )
                        )
                    }
                }
                .onError { error ->
                    emitEvent(
                        EmailVerificationEvent.ShowSnackbar(error.toUiText())
                    )
                }
            updateState { copy(isCheckingVerification = false) }
        }
    }

    private fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess { emitEvent(EmailVerificationEvent.NavigateToLogin) }
                .onError { error ->
                    emitEvent(
                        EmailVerificationEvent.ShowSnackbar(error.toUiText())
                    )
                }
        }
    }

    companion object {
        const val TAG = "EmailVerification"
    }

}