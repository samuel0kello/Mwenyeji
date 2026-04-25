package com.samuelokello.mwenyeji.datasources.core.result

sealed interface NetworkResult<out T> {
    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>

    data class Error(
        val error: RemoteError,
    ) : NetworkResult<Nothing>
}

inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<R> =
    when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> this
    }

inline fun <T> NetworkResult<T>.onSuccess(block: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) block(data)
    return this
}

inline fun <T> NetworkResult<T>.onError(block: (RemoteError) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) block(error)
    return this
}
