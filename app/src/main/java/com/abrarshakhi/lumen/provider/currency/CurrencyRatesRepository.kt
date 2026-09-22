package com.abrarshakhi.lumen.provider.currency

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

/**
 * Cached exchange rates.
 *
 * Stale-while-revalidate: a cached table is served immediately even past its TTL, and a
 * refresh happens alongside. Reference rates move by fractions of a percent in a day, so
 * showing yesterday's number instantly beats showing a spinner — and showing nothing at all
 * when the device is offline would be worse still.
 */
class CurrencyRatesRepository(
    private val client: ExchangeRatesClient,
    private val ttl: Duration = DEFAULT_TTL,
    private val now: () -> Long = System::currentTimeMillis,
) {

    private data class Entry(val table: RateTable, val fetchedAtMillis: Long)

    private val lock = Mutex()
    private val cache = mutableMapOf<String, Entry>()

    data class Rates(val table: RateTable, val isStale: Boolean)

    /** Whatever is cached, without touching the network. Used to paint an instant answer. */
    suspend fun cachedRates(base: String): Rates? = lock.withLock {
        cache[base]?.let { entry ->
            Rates(entry.table, isStale = (now() - entry.fetchedAtMillis) >= ttl.inWholeMilliseconds)
        }
    }

    suspend fun ratesFor(base: String): Rates? {
        val cached = lock.withLock { cache[base] }
        val fresh = cached != null && (now() - cached.fetchedAtMillis) < ttl.inWholeMilliseconds

        if (fresh) return Rates(cached!!.table, isStale = false)

        val fetched = client.fetch(base)
        if (fetched != null) {
            lock.withLock { cache[base] = Entry(fetched, now()) }
            return Rates(fetched, isStale = false)
        }

        // Network failed. A stale answer, clearly marked, is more useful than none.
        return cached?.let { Rates(it.table, isStale = true) }
    }

    private companion object {
        val DEFAULT_TTL = 12.hours
    }
}
