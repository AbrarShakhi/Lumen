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

/**
 * The single shared HTTP client.
 *
 * One client for the whole app because each one owns a connection pool and dispatcher;
 * creating them per call site is a well-known way to leak sockets and threads.
 *
 * OkHttp rather than the `android` engine: mature connection pooling and HTTP/2, and it is
 * the engine almost every Android networking report is written against.
 */
object HttpClientFactory {

    val json: Json = Json {
        // Remote APIs add fields without warning; failing to parse a response because of a
        // field Lumen does not use would be needlessly brittle.
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun create(): HttpClient = HttpClient(OkHttp) {
        // A failed request degrades one provider; it must never surface as a crash.
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
                // INFO, never HEADERS or ALL: API keys travel in headers, and logging them
                // would write user secrets into logcat where any app with log access could
                // read them.
                level = LogLevel.INFO
            }
        }
    }
}
