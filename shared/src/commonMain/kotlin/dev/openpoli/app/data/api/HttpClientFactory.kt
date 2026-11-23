package dev.openpoli.app.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Each platform provides its engine: OkHttp, Darwin or CIO. */
private const val REQUEST_TIMEOUT_MILLIS = 20_000L

expect fun httpClientEngine(): HttpClientEngineFactory<*>

fun createHttpClient(): HttpClient =
    HttpClient(httpClientEngine()) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    explicitNulls = false
                },
            )
        }
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
        }
        expectSuccess = false
    }
