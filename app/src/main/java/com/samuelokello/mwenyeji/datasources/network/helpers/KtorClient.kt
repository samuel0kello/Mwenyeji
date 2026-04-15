package com.samuelokello.mwenyeji.datasources.network.helpers

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

val JSON =
    Json {
        encodeDefaults = true
        explicitNulls = false
        isLenient = true
        ignoreUnknownKeys = true
    }

fun createHttpClient(engine: HttpClientEngineFactory<HttpClientEngineConfig>): HttpClient =
    HttpClient(engine) {
        install(ContentNegotiation) {
            json(
                json = JSON,
            )
        }
        install(Logging) {
            level = LogLevel.ALL
            logger =
                object : Logger {
                    override fun log(message: String) {
                        print(message)
                    }
                }
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 60_000L
            connectTimeoutMillis = 60_000L
            socketTimeoutMillis = 60_000L
        }

        install(DefaultRequest) {
            headers {
                header("Accept", "application/json")
                header("Content-Type", "application/json")
            }
        }
    }
