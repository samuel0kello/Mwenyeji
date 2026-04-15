package com.samuelokello.mwenyeji.datasources.network.helpers

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import kotlin.coroutines.CoroutineContext

@Serializable
class EmptyBody

suspend inline fun <reified T, reified M> HttpClient.postRequest(
    data: T? = null,
    urlString: String,
    coroutineContext: CoroutineContext,
    apiKey: String? = null,
): NetworkResult<M> =
    safeApiCall(coroutineContext) {
        post(urlString = urlString) {
            if (data != null) {
                setBody(data)
            } else {
                setBody(EmptyBody())
            }

            apiKey?.let { key ->
                header("APIKEY", key)
            }
        }
    }

suspend inline fun <reified M> HttpClient.getRequest(
    urlString: String,
    coroutineContext: CoroutineContext,
    apikey: String?,
): NetworkResult<M> =
    safeApiCall(coroutineContext) {
        get(urlString = urlString) {
            apikey?.let { key ->
                header("SKEY", key)
            }
        }
    }
