package com.samuelokello.mwenyeji.datasources.core.network.helpers

import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import com.samuelokello.mwenyeji.datasources.core.result.RemoteError
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import kotlin.coroutines.CoroutineContext

suspend inline fun <reified T> safeApiCall(
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend () -> HttpResponse,
): NetworkResult<T> =
    withContext(context) {
        try {
            val response = block()
            when (response.status.value) {
                in 200..299 -> NetworkResult.Success(response.body())
                in 400..499 -> NetworkResult.Error(HttpErrorMapper.map(response.status.value))
                in 500..599 -> NetworkResult.Error(RemoteError.ServerError)
                else -> NetworkResult.Error(RemoteError.Unknown("Unexpected: ${response.status.value}"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            NetworkResult.Error(RemoteError.NoNetwork)
        } catch (e: Throwable) {
            NetworkResult.Error(RemoteError.Unknown(e.message ?: "Network error"))
        }
    }
