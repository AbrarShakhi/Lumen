package com.abrarshakhi.lumen.provider.currency

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/** Rates for one base currency, as fetched. */
@Serializable
data class RateTable(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)

/**
 * Fetches exchange rates.
 *
 * Uses Frankfurter, which serves European Central Bank reference rates without an API key —
 * so currency conversion works out of the box rather than being gated behind setup the way
 * AI answers are.
 */
class ExchangeRatesClient(
    private val client: HttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL,
) {

    /** Returns null on any failure; the caller falls back to whatever it has cached. */
    suspend fun fetch(base: String): RateTable? = runCatching {
        val response = client.get("$baseUrl/latest?base=$base")
        if (!response.status.isSuccess()) return null
        response.body<RateTable>()
    }.getOrNull()

    companion object {
        const val DEFAULT_BASE_URL = "https://api.frankfurter.app"

        /**
         * Currencies Lumen will recognise in a query.
         *
         * Hard-coded rather than discovered, because the parser needs to decide whether a
         * three-letter word is a currency *before* any network call — offline, and on every
         * keystroke.
         */
        val SUPPORTED: Set<String> = setOf(
            "AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP",
            "HKD", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN", "MYR",
            "NOK", "NZD", "PHP", "PLN", "RON", "SEK", "SGD", "THB", "TRY", "USD", "ZAR",
        )
    }
}
