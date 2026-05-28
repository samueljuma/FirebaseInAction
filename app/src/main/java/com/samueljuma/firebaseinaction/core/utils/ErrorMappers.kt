package com.samueljuma.firebaseinaction.core.utils

import com.samueljuma.firebaseinaction.R


// Top-level dispatcher
fun DataError.toUiText(): UiText = when (this) {
    is DataError.Auth -> this.toUiText()
    is DataError.Firestore -> this.toUiText()
    is DataError.Local -> this.toUiText()
    is DataError.Storage -> this.toUiText()
}

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
    DataError.Auth.EMAIL_NOT_VERIFIED ->
        UiText.StringResource(R.string.error_email_not_verified)
    DataError.Auth.INVALID_EMAIL ->
        UiText.StringResource(R.string.error_invalid_email)
    DataError.Auth.INVALID_PASSWORD ->
        UiText.StringResource(R.string.error_invalid_password)
    DataError.Auth.CANCELLED ->
        UiText.DynamicString("")
    DataError.Auth.GOOGLE_SIGN_IN_FAILED ->
        UiText.StringResource(R.string.error_google_sign_in_failed)
    DataError.Auth.UNKNOWN ->
        UiText.StringResource(R.string.error_unknown)
}

fun DataError.Firestore.toUiText(): UiText = when (this) {
    DataError.Firestore.PERMISSION_DENIED ->
        UiText.StringResource(R.string.error_permission_denied)
    DataError.Firestore.NOT_FOUND ->
        UiText.StringResource(R.string.error_not_found)
    DataError.Firestore.ALREADY_EXISTS ->
        UiText.StringResource(R.string.error_already_exists)
    DataError.Firestore.RESOURCE_EXHAUSTED ->
        UiText.StringResource(R.string.error_resource_exhausted)
    DataError.Firestore.NETWORK_ERROR ->
        UiText.StringResource(R.string.error_network)
    DataError.Firestore.UNKNOWN ->
        UiText.StringResource(R.string.error_unknown)
}

fun DataError.Local.toUiText(): UiText = when (this) {
    DataError.Local.DISK_FULL ->
        UiText.StringResource(R.string.error_disk_full)
    DataError.Local.UNKNOWN ->
        UiText.StringResource(R.string.error_unknown)
}

fun DataError.Storage.toUiText(): UiText = when (this) {
    DataError.Storage.OBJECT_NOT_FOUND ->
        UiText.StringResource(R.string.error_object_not_found)
    DataError.Storage.BUCKET_NOT_FOUND ->
        UiText.StringResource(R.string.error_unknown)
    DataError.Storage.UNAUTHORIZED ->
        UiText.StringResource(R.string.error_permission_denied)
    DataError.Storage.QUOTA_EXCEEDED ->
        UiText.StringResource(R.string.error_resource_exhausted)
    DataError.Storage.NETWORK_ERROR ->
        UiText.StringResource(R.string.error_network)
    DataError.Storage.FILE_TOO_LARGE ->
        UiText.StringResource(R.string.error_file_too_large)
    DataError.Storage.UNKNOWN ->
        UiText.StringResource(R.string.error_unknown)
}