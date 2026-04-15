package com.samuelokello.mwenyeji.datasources.network.helpers

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okio.IOException
import kotlin.coroutines.CoroutineContext

@Serializable
data class ApiResponseDto(
    @SerialName("Status")
    val status: Int? = null,
    @SerialName("Message")
    val message: String? = null,
)

sealed interface NetworkResult<out T> {
    data class Success<T>(
        val data: T,
    ) : NetworkResult<T>

    data class Error(
        val errorMessage: String,
        val statusCode: Int = 500,
    ) : NetworkResult<Nothing>
}

suspend inline fun <reified T> safeApiCall(
    context: CoroutineContext = Dispatchers.IO,
    crossinline block: suspend () -> HttpResponse,
): NetworkResult<T> {
    val fallbackMessage = "Opps! Something went wrong. Please try again later"
    return withContext(context) {
        try {
            val response = block()

            when (response.status) {
                in listOf(HttpStatusCode.OK, HttpStatusCode.Created, HttpStatusCode.Accepted) -> {
                    val apiResponse = response.body<ApiResponseDto>()
                    when (apiResponse.status) {
                        200 -> {
                            NetworkResult.Success(response.body())
                        }

                        null -> {
                            NetworkResult.Success(response.body())
                        }

                        else -> {
                            NetworkResult.Error(
                                errorMessage = fallbackMessage,
                                statusCode = apiResponse.status,
                            )
                        }
                    }
                }

                HttpStatusCode.NotFound -> {
                    response.processError()
                }

                HttpStatusCode.PaymentRequired -> {
                    response.processError()
                }

                HttpStatusCode.UnprocessableEntity -> {
                    response.processError()
                }

                HttpStatusCode.SwitchingProtocols -> {
                    response.processError()
                }

                HttpStatusCode.Forbidden -> {
                    response.processError()
                }

                HttpStatusCode.Unauthorized -> {
                    response.processError()
                }

                HttpStatusCode.BadRequest -> {
                    response.processError()
                }

                HttpStatusCode.fromValue(-1) -> {
                    response.processError()
                }

                else -> {
                    println("error occurred, code: ${response.status.value}")
                    NetworkResult.Error(fallbackMessage)
                }
            }
        } catch (e: IOException) {
            NetworkResult.Error("No internet connection!! Please check your internet connection and try again")
        } catch (e: Exception) {
            // if (e is CancellationException) throw e
            println("exception occurred: ${e.cause}")

            NetworkResult.Error(fallbackMessage)
        }
    }
}

/**
 * The logic for processing errors will most likely change
 */
suspend fun <T> HttpResponse.processError(): NetworkResult<T> {
    val fallbackMessage = "Opps! Something went wrong processing. Please try again later"
    return try {
        val apiResponse = body<ApiResponseDto>()
        NetworkResult.Error(
            errorMessage = fallbackMessage,
            statusCode = apiResponse.status ?: -1,
        )
    } catch (e: Exception) {
        println("exception occurred: ${e.cause}")
        NetworkResult.Error(
            errorMessage = fallbackMessage,
            statusCode = 500,
        )
    }
}
