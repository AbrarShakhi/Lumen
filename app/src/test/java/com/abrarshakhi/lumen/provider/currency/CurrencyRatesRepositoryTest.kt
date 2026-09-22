package com.abrarshakhi.lumen.provider.currency

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours

/**
 * Exercises the Ktor client with `MockEngine`, so the caching policy is tested on the JVM
 * with no network and no device.
 */
class CurrencyRatesRepositoryTest {

    private val body = """{"base":"USD","date":"2026-09-19","rates":{"EUR":0.9,"GBP":0.78}}"""

    private fun client(handler: MockEngine) = HttpClient(handler) {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    private fun okEngine(counter: IntArray) = MockEngine {
        counter[0]++
        respond(
            content = body,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
        )
    }

    @Test
    fun `rates are fetched and converted`() = runTest {
        val calls = intArrayOf(0)
        val repository = CurrencyRatesRepository(ExchangeRatesClient(client(okEngine(calls)), ""))

        val rates = assertNotNull(repository.ratesFor("USD"))

        assertEquals(0.9, rates.table.rates["EUR"])
        assertTrue(!rates.isStale)
    }

    @Test
    fun `a fresh cache is not refetched`() = runTest {
        val calls = intArrayOf(0)
        val repository = CurrencyRatesRepository(ExchangeRatesClient(client(okEngine(calls)), ""))

        repository.ratesFor("USD")
        repository.ratesFor("USD")
        repository.ratesFor("USD")

        assertEquals(1, calls[0], "a fresh cache must not hit the network again")
    }

    @Test
    fun `an expired cache is refetched`() = runTest {
        val calls = intArrayOf(0)
        var clock = 0L
        val repository = CurrencyRatesRepository(
            client = ExchangeRatesClient(client(okEngine(calls)), ""),
            ttl = 12.hours,
            now = { clock },
        )

        repository.ratesFor("USD")
        clock += 13.hours.inWholeMilliseconds
        repository.ratesFor("USD")

        assertEquals(2, calls[0])
    }

    @Test
    fun `a stale rate is served when the network fails`() = runTest {
        // Offline, yesterday's reference rate is far more useful than nothing — as long as
        // it is labelled.
        var failing = false
        var clock = 0L
        val engine = MockEngine {
            if (failing) {
                respondError(HttpStatusCode.ServiceUnavailable)
            } else {
                respond(
                    content = body,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }
        val repository = CurrencyRatesRepository(
            client = ExchangeRatesClient(client(engine), ""),
            ttl = 12.hours,
            now = { clock },
        )

        repository.ratesFor("USD")
        failing = true
        clock += 24.hours.inWholeMilliseconds

        val stale = assertNotNull(repository.ratesFor("USD"))
        assertTrue(stale.isStale, "a fallback rate must be reported as stale")
        assertEquals(0.9, stale.table.rates["EUR"])
    }

    @Test
    fun `nothing cached and no network yields nothing`() = runTest {
        val engine = MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }
        val repository = CurrencyRatesRepository(ExchangeRatesClient(client(engine), ""))

        assertNull(repository.ratesFor("USD"), "better to say nothing than to invent a rate")
    }
}
