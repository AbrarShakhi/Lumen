package com.abrarshakhi.lumen.provider.currency

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

@Serializable
data class RateTable(
    val base: String,
    val date: String,
    val rates: Map<String, Double>,
)

class ExchangeRatesClient(
    private val client: HttpClient,
    private val baseUrl: String = DEFAULT_BASE_URL,
) {

    suspend fun fetch(base: String): RateTable? = runCatching {
        val response = client.get("$baseUrl/latest?base=$base")
        if (!response.status.isSuccess()) return null
        response.body<RateTable>()
    }.getOrNull()

    companion object {
        const val DEFAULT_BASE_URL = "https://api.frankfurter.app"

        val SUPPORTED: Set<String> = setOf(
            "AUD", "BGN", "BRL", "CAD", "CHF", "CNY", "CZK", "DKK", "EUR", "GBP",
            "HKD", "HUF", "IDR", "ILS", "INR", "ISK", "JPY", "KRW", "MXN", "MYR",
            "NOK", "NZD", "PHP", "PLN", "RON", "SEK", "SGD", "THB", "TRY", "USD", "ZAR",
        )
    }
}
