package com.abrarshakhi.lumen.core.platform.contacts

import com.abrarshakhi.lumen.core.domain.repository.Contact
import com.abrarshakhi.lumen.core.domain.repository.ContactRepository
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class CachedContactRepository(
    private val load: suspend () -> List<Contact>?,
) : ContactRepository {

    private val loadLock = Mutex()

    @Volatile
    private var cache: List<Contact>? = null

    override suspend fun contacts(): List<Contact> {
        cache?.let { return it }
        return loadLock.withLock {
            cache ?: run {
                val loaded = load()
                loaded?.also { cache = it } ?: emptyList()
            }
        }
    }

    override fun invalidate() {
        cache = null
    }
}
