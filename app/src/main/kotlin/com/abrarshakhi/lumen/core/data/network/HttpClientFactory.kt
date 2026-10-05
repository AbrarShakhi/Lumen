package com.abrarshakhi.lumen.core.data.network

import com.abrarshakhi.lumen.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

object HttpClientFactory {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun create(): HttpClient = HttpClient(OkHttp) {
        expectSuccess = false

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 10.seconds.inWholeMilliseconds
            requestTimeoutMillis = 30.seconds.inWholeMilliseconds
            socketTimeoutMillis = 30.seconds.inWholeMilliseconds
        }

        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 2)
            exponentialDelay()
        }

        defaultRequest {
            header(HttpHeaders.UserAgent, "Lumen/${BuildConfig.VERSION_NAME}")
        }

        if (BuildConfig.DEBUG) {
            install(Logging) {
                level = LogLevel.INFO
            }
        }
    }
}
