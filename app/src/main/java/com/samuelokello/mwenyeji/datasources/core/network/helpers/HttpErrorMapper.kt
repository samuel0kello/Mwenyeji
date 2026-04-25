package com.samuelokello.mwenyeji.datasources.core.network.helpers

import com.samuelokello.mwenyeji.datasources.core.result.RemoteError

@PublishedApi
internal object HttpErrorMapper {
    fun map(statusCode: Int): RemoteError =
        when (statusCode) {
            400 -> RemoteError.BadRequest
            401 -> RemoteError.Unauthorized
            403 -> RemoteError.Forbidden
            404 -> RemoteError.NotFound
            408 -> RemoteError.DeadlineExceeded
            in 500..599 -> RemoteError.ServerError
            else -> RemoteError.Unknown("HTTP $statusCode")
        }
}
