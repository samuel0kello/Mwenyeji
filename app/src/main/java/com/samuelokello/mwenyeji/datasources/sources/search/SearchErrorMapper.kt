package com.samuelokello.mwenyeji.datasources.sources.search

import com.samuelokello.mwenyeji.datasources.core.result.RemoteError
import java.io.IOException

internal object SearchErrorMapper {
    fun map(throwable: Throwable): RemoteError =
        when (throwable) {
            is IOException -> RemoteError.NoNetwork
            else -> RemoteError.Unknown(throwable.message ?: "Search failed")
        }
}
