package com.samueljuma.firebaseinaction.core.utils

interface AppError

sealed interface DataError : AppError {

    enum class Auth : DataError {
        WEAK_PASSWORD,                // < 6 characters
        INVALID_CREDENTIALS,          // wrong email/password
        USER_ALREADY_EXISTS,          // email taken on sign up
        USER_NOT_FOUND,               // account deleted/doesn't exist
        EMAIL_NOT_VERIFIED,           // trying to access before verifying
        TOO_MANY_REQUESTS,            // brute force lockout
        INVALID_EMAIL,
        INVALID_PASSWORD,
        CANCELLED,
        NO_GOOGLE_ACCOUNT,
        GOOGLE_SIGN_IN_FAILED,
        NETWORK_ERROR,                // no internet during auth
        UNKNOWN,                       // safety net
    }

    enum class Firestore : DataError {
        PERMISSION_DENIED,            // security rules blocked
        NOT_FOUND,                    // document doesn't exist
        ALREADY_EXISTS,               // document collision
        RESOURCE_EXHAUSTED,           // quota exceeded
        NETWORK_ERROR,
        UNKNOWN
    }

    enum class Storage : DataError {
        OBJECT_NOT_FOUND,             // file doesn't exist
        BUCKET_NOT_FOUND,             // misconfigured bucket
        UNAUTHORIZED,                 // security rules blocked
        QUOTA_EXCEEDED,
        NETWORK_ERROR,
        FILE_TOO_LARGE,
        UNKNOWN
    }

    enum class Local : DataError {
        DISK_FULL,
        UNKNOWN
    }
}