package com.samuelokello.mwenyeji.data.helpers

import com.samuelokello.mwenyeji.datasources.network.helpers.NetworkResult

sealed interface DataResult<out T> {
    data class Success<T>(
        val data: T,
    ) : DataResult<T>

    data class Error(
        val errorMessage: String,
    ) : DataResult<Nothing>
}

fun <T> DataResult<T>.onSuccess(block: (T) -> Unit): DataResult<T> {
    if (this is DataResult.Success) {
        block(this.data)
    }
    return this
}

fun <T> DataResult<T>.onError(block: (String) -> Unit): DataResult<T> {
    if (this is DataResult.Error) {
        block(this.errorMessage)
    }
    return this
}

suspend fun <T, R> NetworkResult<T>.map(transform: suspend (T) -> R): DataResult<R> =
    when (this) {
        is NetworkResult.Error -> DataResult.Error(errorMessage)
        is NetworkResult.Success -> DataResult.Success(transform(data))
    }
