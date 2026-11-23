package dev.openpoli.app.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Each platform provides its engine: OkHttp, Darwin or CIO. */
expect fun httpClientEngine(): HttpClientEngineFactory<*>

fun createHttpClient(): HttpClient = HttpClient(httpClientEngine()) {
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            },
        )
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 20_000
    }
    expectSuccess = false
}
