package com.samuelokello.mwenyeji.data.helpers

import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.core.result.RemoteError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun RemoteError.toDomain(): DomainError =
    when (this) {
        RemoteError.InvalidCredentials -> DomainError.InvalidCredentials

        RemoteError.UserNotFound -> DomainError.UserNotFound

        RemoteError.EmailAlreadyInUse,
        RemoteError.AccountCollision,
        -> DomainError.EmailAlreadyInUse

        RemoteError.WeakPassword -> DomainError.WeakPassword

        RemoteError.UserDisabled -> DomainError.AccountDisabled

        RemoteError.RequiresRecentLogin -> DomainError.SessionExpired

        RemoteError.NoNetwork -> DomainError.NoConnection

        RemoteError.Unavailable,
        RemoteError.DeadlineExceeded,
        RemoteError.ServerError,
        -> DomainError.ServiceUnavailable

        RemoteError.PermissionDenied,
        RemoteError.Forbidden,
        RemoteError.Unauthorized,
        -> DomainError.NotAuthorized

        RemoteError.NotFound,
        RemoteError.Cancelled,
        RemoteError.AlreadyExists,
        RemoteError.BadRequest,
        -> DomainError.Unknown(technicalMessage)

        is RemoteError.Unknown -> DomainError.Unknown(technicalMessage)
    }

/** Cross from infrastructure result to domain result. No transform. */
internal fun <T> NetworkResult<T>.toDataResult(): DataResult<T> =
    when (this) {
        is NetworkResult.Success -> DataResult.Success(data)
        is NetworkResult.Error -> DataResult.Error(error.toDomain())
    }

/** Cross layers and transform in one step. */
internal inline fun <T, R> NetworkResult<T>.toDataResult(transform: (T) -> R): DataResult<R> =
    when (this) {
        is NetworkResult.Success -> DataResult.Success(transform(data))
        is NetworkResult.Error -> DataResult.Error(error.toDomain())
    }

/** Suspending variant for transforms that need to call other suspending code. */
internal suspend inline fun <T, R> NetworkResult<T>.toDataResultAwait(crossinline transform: suspend (T) -> R): DataResult<R> =
    when (this) {
        is NetworkResult.Success -> DataResult.Success(transform(data))
        is NetworkResult.Error -> DataResult.Error(error.toDomain())
    }

fun <T> Flow<NetworkResult<T>>.toDataResultFlow(): Flow<DataResult<T>> = map { it.toDataResult() }

fun <T, R> Flow<NetworkResult<T>>.toDataResultFlow(transform: (T) -> R): Flow<DataResult<R>> = map { it.toDataResult(transform) }
