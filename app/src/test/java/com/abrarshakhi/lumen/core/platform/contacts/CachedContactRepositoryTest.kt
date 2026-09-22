package com.abrarshakhi.lumen.core.platform.contacts

import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.Contact
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards a bug that actually shipped into a build: contact search stayed permanently empty
 * after granting the permission, because the failed read performed at startup had been
 * cached as though it were a legitimate "no contacts" answer.
 */
class CachedContactRepositoryTest {

    private fun contact(name: String) = Contact(
        id = name,
        lookupKey = name,
        displayName = name,
        searchable = SearchableText.of(name),
        phoneNumbers = emptyList(),
        photoUri = null,
    )

    @Test
    fun `a failed read is not cached, so a later grant works`() = runTest {
        var granted = false
        val repository = CachedContactRepository {
            if (granted) listOf(contact("Ada")) else null
        }

        assertTrue(repository.contacts().isEmpty(), "no permission yet")

        granted = true

        assertEquals(
            listOf("Ada"),
            repository.contacts().map { it.displayName },
            "granting the permission must make contacts appear without restarting the app",
        )
    }

    @Test
    fun `a successful read is cached`() = runTest {
        var loads = 0
        val repository = CachedContactRepository {
            loads++
            listOf(contact("Ada"))
        }

        repeat(3) { repository.contacts() }

        assertEquals(1, loads, "repeated searches must not re-query the ContentResolver")
    }

    @Test
    fun `a genuinely empty address book is cached`() = runTest {
        // Distinct from a failure: this is a real answer and should not be re-queried.
        var loads = 0
        val repository = CachedContactRepository {
            loads++
            emptyList()
        }

        repeat(3) { repository.contacts() }

        assertEquals(1, loads)
    }

    @Test
    fun `invalidate forces a reload`() = runTest {
        var loads = 0
        val repository = CachedContactRepository {
            loads++
            listOf(contact("Ada"))
        }

        repository.contacts()
        repository.invalidate()
        repository.contacts()

        assertEquals(2, loads)
    }
}
