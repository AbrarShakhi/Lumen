package com.abrarshakhi.lumen.app.di

import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ProviderResults
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the mechanism that makes providers pluggable.
 *
 * Koin has no Dagger-style multibinding, so the registry collects providers with
 * `getAll<SearchProvider>()`. If that ever stops seeing definitions registered under a
 * `bind`, every provider silently disappears and search returns nothing — a failure that
 * looks like "no results" rather than an error. This test turns that into a build failure.
 */
class ProviderRegistrationTest {

    private class StubProvider(name: String, order: Int) : SearchProvider {
        override val id = ProviderId(name)
        override val metadata = ProviderMetadata(
            displayName = TextValue.Raw(name),
            category = ResultCategory.App,
            order = order,
        )
        override fun search(query: SearchQuery): Flow<ProviderResults> =
            flowOf(ProviderResults.empty(id))
    }

    private class OtherProvider(name: String, order: Int) : SearchProvider by StubProvider(name, order)

    @AfterTest
    fun tearDown() = stopKoin()

    @Test
    fun `getAll finds providers registered under a bind`() {
        val koin = startKoin {
            modules(
                module { single { StubProvider("apps", 10) } bind SearchProvider::class },
                module { single { OtherProvider("contacts", 20) } bind SearchProvider::class },
            )
        }.koin

        val found = koin.getAll<SearchProvider>()

        assertEquals(
            setOf("apps", "contacts"),
            found.map { it.id.value }.toSet(),
            "getAll must see every provider bound to SearchProvider",
        )
    }

    @Test
    fun `a provider registered without bind is invisible to getAll`() {
        // Documents the failure mode: forgetting `bind` is silent, which is exactly why
        // the test above exists.
        val koin = startKoin {
            modules(module { single { StubProvider("apps", 10) } })
        }.koin

        assertTrue(
            koin.getAll<SearchProvider>().isEmpty(),
            "without `bind`, getAll cannot see the provider — every provider module needs it",
        )
    }
}
