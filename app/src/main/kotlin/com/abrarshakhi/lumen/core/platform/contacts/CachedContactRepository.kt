package com.abrarshakhi.lumen.core.platform.contacts

import com.abrarshakhi.lumen.core.domain.repository.Contact
import com.abrarshakhi.lumen.core.domain.repository.ContactRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Loads contacts once and keeps them in memory.
 *
 * Loading is lazy rather than eager because the provider is not dispatched at all until
 * `READ_CONTACTS` is granted; the first search after granting pays the query, and every
 * search after that is an in-memory filter.
 *
 * Takes a loader function rather than the data source itself so the caching policy — the
 * part with the interesting failure mode — is unit-testable without Android.
 */
class CachedContactRepository(
    private val load: suspend () -> List<Contact>?,
) : ContactRepository {

    private val loadLock = Mutex()

    @Volatile
    private var cache: List<Contact>? = null

    override suspend fun contacts(): List<Contact> {
        cache?.let { return it }
        return loadLock.withLock {
            // Re-check inside the lock: providers may race on the first keystroke.
            cache ?: run {
                // Only a *successful* read is cached. Caching failures was a real bug:
                // warmUp() runs at startup before READ_CONTACTS is granted, so the failed
                // read cached an empty list and contact search stayed dead even after the
                // user granted the permission.
                val loaded = load()
                loaded?.also { cache = it } ?: emptyList()
            }
        }
    }

    override fun invalidate() {
        cache = null
    }
}
