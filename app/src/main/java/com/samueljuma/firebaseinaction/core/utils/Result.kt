package com.samueljuma.firebaseinaction.core.utils

sealed interface Result<out D, out E : AppError> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : AppError>(val error: E) : Result<Nothing, E>
}


inline fun <D, E : AppError> Result<D, E>.onSuccess(
    action: (D) -> Unit
): Result<D, E> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <D, E : AppError> Result<D, E>.onError(
    action: (E) -> Unit
): Result<D, E> {
    if (this is Result.Error) action(error)
    return this
}

inline fun <D, E : AppError, R> Result<D, E>.map(
    transform: (D) -> R
): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
}