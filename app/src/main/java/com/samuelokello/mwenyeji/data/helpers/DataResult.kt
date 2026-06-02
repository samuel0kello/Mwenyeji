package com.samuelokello.mwenyeji.data.helpers

sealed interface DataResult<out T> {
    data class Success<T>(
        val data: T,
    ) : DataResult<T>

    data class Error(
        val error: DomainError,
    ) : DataResult<Nothing>
}

/** Transform the success value without crossing layer boundaries. */
inline fun <T, R> DataResult<T>.map(transform: (T) -> R): DataResult<R> =
    when (this) {
        is DataResult.Success -> DataResult.Success(transform(data))
        is DataResult.Error -> this
    }

inline fun <T> DataResult<T>.onSuccess(block: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) block(data)
    return this
}

inline fun <T> DataResult<T>.onError(block: (DomainError) -> Unit): DataResult<T> {
    if (this is DataResult.Error) block(error)
    return this
}

/** Unwrap with a fallback. Useful at the UI layer. */
inline fun <T> DataResult<T>.getOrElse(onError: (DomainError) -> T): T =
    when (this) {
        is DataResult.Success -> data
        is DataResult.Error -> onError(error)
    }
