package com.abrarshakhi.lumen.core.domain.search

import app.cash.turbine.test
import com.abrarshakhi.lumen.core.domain.rank.DefaultResultRanker
import com.abrarshakhi.lumen.core.domain.rank.UsageSnapshot
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/**
 * The specification of the search architecture.
 *
 * These assert the properties that make search feel instant: fast providers render without
 * waiting for slow ones, a keystroke cancels in-flight work, a slow provider is bounded,
 * and one failing provider cannot take down the rest. If any of these break, the app
 * regresses in a way that is very hard to notice by hand.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchEngineTest {

    // --- Test doubles -------------------------------------------------------------------

    private class FakeProvider(
        name: String,
        override val metadata: ProviderMetadata,
        private val delayMillis: Long = 0,
        private val failWith: Throwable? = null,
        private val results: (SearchQuery) -> List<SearchResult> = { emptyList() },
    ) : SearchProvider {
        override val id = ProviderId(name)
        var searchCount = 0
            private set

        override fun search(query: SearchQuery): Flow<ProviderResults> = flow {
            searchCount++
            if (delayMillis > 0) delay(delayMillis)
            failWith?.let { throw it }
            emit(ProviderResults(id, results(query)))
        }
    }

    private fun metadata(order: Int, timeoutMillis: Long = 800, debounceMillis: Long = 0) =
        ProviderMetadata(
            displayName = TextValue.Raw("fake"),
            category = ResultCategory.App,
            order = order,
            timeout = timeoutMillis.milliseconds,
            debounce = debounceMillis.milliseconds,
        )

    private fun result(id: String, title: String = id, score: Float = 1f) = SearchResult(
        id = ResultId(id),
        providerId = ProviderId("fake"),
        title = title,
        icon = IconSource.Vector(LumenIcon.App),
        category = ResultCategory.App,
        score = score,
        actions = ResultActions(
            primary = ResultAction(
                id = "open",
                label = TextValue.Raw("Open"),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Handled },
            ),
        ),
    )

    private fun engine(vararg providers: SearchProvider) = SearchEngine(
        registry = DefaultSearchProviderRegistry(providers.toList()),
        ranker = DefaultResultRanker(),
        triggers = DefaultTriggerResolver(),
        gate = AllowAllGate,
        usage = MutableStateFlow(UsageSnapshot.Empty),
    )

    private object AllowAllGate : ProviderGate {
        override fun evaluate(providers: List<SearchProvider>, query: SearchQuery) =
            GateDecision(dispatch = if (query.isBlank) emptyList() else providers, permissionRequests = emptyList())
    }

    // --- Specification ------------------------------------------------------------------

    @Test
    fun `a single provider's results are rendered`() = runTest {
        val provider = FakeProvider("apps", metadata(order = 10)) { listOf(result("a", "Alpha")) }

        engine(provider).observe(MutableStateFlow("alp")).test {
            val settled = awaitSettled()
            assertEquals(listOf("Alpha"), settled.flatResults.map { it.title })
        }
    }

    @Test
    fun `fast providers render before slow ones finish`() = runTest {
        val fast = FakeProvider("fast", metadata(order = 10)) { listOf(result("f", "Fast")) }
        val slow = FakeProvider("slow", metadata(order = 20), delayMillis = 500) {
            listOf(result("s", "Slow"))
        }

        engine(fast, slow).observe(MutableStateFlow("q")).test {
            // The point of the architecture: something is on screen with the slow
            // provider still outstanding, rather than waiting for the slowest.
            var sawPartial = false
            var settled: SearchResults? = null
            while (settled == null) {
                val emission = awaitItem()
                if (emission.flatResults.any { it.title == "Fast" } && !emission.isSettled) {
                    sawPartial = true
                }
                if (emission.isSettled && emission.flatResults.size == 2) settled = emission
            }
            assertTrue(sawPartial, "fast provider's results should render while slow one is pending")
            assertEquals(setOf("Fast", "Slow"), settled.flatResults.map { it.title }.toSet())
        }
    }

    @Test
    fun `a provider exceeding its timeout does not stall the search`() = runTest {
        val prompt = FakeProvider("prompt", metadata(order = 10)) { listOf(result("p", "Prompt")) }
        val tooSlow = FakeProvider(
            "tooSlow",
            metadata(order = 20, timeoutMillis = 100),
            delayMillis = 5_000,
        ) { listOf(result("t", "TooSlow")) }

        engine(prompt, tooSlow).observe(MutableStateFlow("q")).test {
            val settled = awaitSettled()
            assertEquals(listOf("Prompt"), settled.flatResults.map { it.title })
        }
    }

    @Test
    fun `one failing provider does not kill the others`() = runTest {
        val healthy = FakeProvider("ok", metadata(order = 10)) { listOf(result("o", "Healthy")) }
        val broken = FakeProvider(
            "broken",
            metadata(order = 20),
            failWith = IllegalStateException("boom"),
        )

        engine(healthy, broken).observe(MutableStateFlow("q")).test {
            val settled = awaitSettled()
            assertEquals(listOf("Healthy"), settled.flatResults.map { it.title })
            assertTrue(settled.failures.containsKey(ProviderId("broken")), "failure should be reported")
        }
    }

    @Test
    fun `a blank query dispatches nothing`() = runTest {
        val provider = FakeProvider("apps", metadata(order = 10)) { listOf(result("a")) }

        engine(provider).observe(MutableStateFlow("   ")).test {
            val emission = awaitItem()
            assertTrue(emission.isEmpty)
            assertEquals(0, provider.searchCount, "blank queries must not reach providers")
        }
    }

    @Test
    fun `debounce delays dispatch without blocking undebounced providers`() = runTest {
        val instant = FakeProvider("instant", metadata(order = 10)) { listOf(result("i", "Instant")) }
        val debounced = FakeProvider(
            "debounced",
            metadata(order = 20, debounceMillis = 300),
        ) { listOf(result("d", "Debounced")) }

        engine(instant, debounced).observe(MutableStateFlow("q")).test {
            val first = awaitItem()
            // Instant provider must not be held back by the debounced one's delay.
            assertTrue(
                first.isEmpty || first.flatResults.none { it.title == "Debounced" },
                "debounced provider should not have emitted yet",
            )
            val settled = awaitSettled()
            assertEquals(setOf("Instant", "Debounced"), settled.flatResults.map { it.title }.toSet())
        }
    }

    private suspend fun app.cash.turbine.ReceiveTurbine<SearchResults>.awaitSettled(): SearchResults {
        while (true) {
            val emission = awaitItem()
            if (emission.isSettled) return emission
        }
    }
}
