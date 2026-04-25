package com.samuelokello.mwenyeji.datasources.core.network.helpers

import com.samuelokello.mwenyeji.datasources.core.result.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

suspend inline fun <reified R> HttpClient.getJson(
    urlString: String,
    context: CoroutineContext = Dispatchers.IO,
    crossinline configure: HttpRequestBuilder.() -> Unit = {},
): NetworkResult<R> =
    safeApiCall(context) {
        get(urlString) { configure() }
    }

suspend inline fun <reified B, reified R> HttpClient.postJson(
    urlString: String,
    body: B? = null,
    context: CoroutineContext = Dispatchers.IO,
): NetworkResult<R> =
    safeApiCall(context) {
        post(urlString) { body?.let(::setBody) }
    }
