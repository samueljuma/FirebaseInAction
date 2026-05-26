package com.samueljuma.firebaseinaction.core.utils

import com.samueljuma.firebaseinaction.R

fun DataError.Auth.toUiText(): UiText = when (this) {
    DataError.Auth.WEAK_PASSWORD ->
        UiText.StringResource(R.string.error_weak_password)
    DataError.Auth.INVALID_CREDENTIALS ->
        UiText.StringResource(R.string.error_invalid_credentials)
    DataError.Auth.USER_ALREADY_EXISTS ->
        UiText.StringResource(R.string.error_user_already_exists)
    DataError.Auth.USER_NOT_FOUND ->
        UiText.StringResource(R.string.error_user_not_found)
    DataError.Auth.TOO_MANY_REQUESTS ->
        UiText.StringResource(R.string.error_too_many_requests)
    DataError.Auth.NETWORK_ERROR ->
        UiText.StringResource(R.string.error_network)
    DataError.Auth.UNKNOWN ->
        UiText.StringResource(R.string.error_unknown)
    DataError.Auth.EMAIL_NOT_VERIFIED ->
        UiText.StringResource(R.string.error_email_not_verified)
    DataError.Auth.INVALID_EMAIL ->
        UiText.StringResource(R.string.error_invalid_email)
    DataError.Auth.INVALID_PASSWORD ->
        UiText.StringResource(R.string.error_invalid_password)
    DataError.Auth.CANCELLED -> UiText.DynamicString("") // silent — user chose to cancel
    DataError.Auth.GOOGLE_SIGN_IN_FAILED ->
        UiText.StringResource(R.string.error_google_sign_in_failed)
}