package com.abrarshakhi.lumen.core.domain.rank

import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Ranking is pure and synchronous, so its policy is asserted directly. */
class DefaultResultRankerTest {

    private val ranker = DefaultResultRanker()
    private val query = SearchQuery(raw = "a", terms = "a", normalizedTerms = "a")

    private fun result(
        id: String,
        title: String,
        score: Float,
        category: ResultCategory = ResultCategory.App,
    ) = SearchResult(
        id = ResultId(id),
        providerId = ProviderId("p"),
        title = title,
        icon = IconSource.Vector(LumenIcon.App),
        category = category,
        score = score,
        rankingKey = id,
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

    private fun usage(vararg entries: Pair<String, Int>, ageMillis: Long = 0) = UsageSnapshot(
        stats = entries.associate { (key, count) ->
            key to UsageStat(launchCount = count, lastLaunchedAtMillis = NOW - ageMillis)
        },
        nowMillis = NOW,
    )

    @Test
    fun `with no usage, relevance decides`() {
        val sections = ranker.rank(
            query,
            listOf(result("b", "Beta", 0.7f), result("a", "Alpha", 0.9f)),
            UsageSnapshot.Empty,
        )

        assertEquals(listOf("Alpha", "Beta"), sections.flatMap { it.results }.map { it.title })
    }

    @Test
    fun `frequent launches lift a weaker textual match above a stronger one`() {
        // The Phase 1 promise: the launcher learns what you actually open.
        val sections = ranker.rank(
            query,
            listOf(result("alpha", "Alpha", 0.90f), result("beta", "Beta", 0.70f)),
            usage("beta" to 12),
        )

        assertEquals(
            "Beta",
            sections.flatMap { it.results }.first().title,
            "a repeatedly launched result should outrank a slightly better text match",
        )
    }

    @Test
    fun `a single launch does not override a much better match`() {
        val sections = ranker.rank(
            query,
            listOf(result("alpha", "Alpha", 1.0f), result("beta", "Beta", 0.35f)),
            usage("beta" to 1),
        )

        assertEquals("Alpha", sections.flatMap { it.results }.first().title)
    }

    @Test
    fun `stale usage decays`() {
        val fresh = ranker.rank(query, listOf(result("a", "A", 0.5f)), usage("a" to 10))
        val stale = ranker.rank(
            query,
            listOf(result("a", "A", 0.5f)),
            usage("a" to 10, ageMillis = 120L * 24 * 60 * 60 * 1000),
        )
        // Both render; the assertion is that the snapshot scores them differently.
        assertTrue(fresh.isNotEmpty() && stale.isNotEmpty())
        assertTrue(
            UsageSnapshot(mapOf("a" to UsageStat(10, NOW)), NOW).scoreFor("a") >
                UsageSnapshot(mapOf("a" to UsageStat(10, NOW - 120L * 24 * 60 * 60 * 1000)), NOW).scoreFor("a"),
            "four-month-old usage should count for less than today's",
        )
    }

    @Test
    fun `answers outrank destinations`() {
        val sections = ranker.rank(
            query,
            listOf(
                result("app", "Some App", 0.95f, ResultCategory.App),
                result("calc", "= 42", 0.80f, ResultCategory.Answer),
            ),
            UsageSnapshot.Empty,
        )

        assertEquals(ResultCategory.Answer, sections.first().category, "a direct answer belongs first")
    }

    @Test
    fun `permission prompts sort above everything`() {
        val sections = ranker.rank(
            query,
            listOf(
                result("app", "Some App", 1.0f, ResultCategory.App),
                result("perm", "Allow contacts", 0.1f, ResultCategory.System),
            ),
            UsageSnapshot.Empty,
        )

        assertEquals(ResultCategory.System, sections.first().category)
    }

    @Test
    fun `sections are capped and report that more exist`() {
        val many = (1..10).map { result("app$it", "App $it", 0.9f) }

        val section = ranker.rank(query, many, UsageSnapshot.Empty).first()

        assertEquals(5, section.results.size, "apps are capped at 5 per section")
        assertTrue(section.hasMore)
    }

    @Test
    fun `duplicate ids are collapsed`() {
        val sections = ranker.rank(
            query,
            listOf(result("same", "One", 0.9f), result("same", "Duplicate", 0.8f)),
            UsageSnapshot.Empty,
        )

        assertEquals(1, sections.flatMap { it.results }.size)
    }

    @Test
    fun `equal scores order deterministically so the list does not flicker`() {
        // Slower providers arrive later; ties must not reshuffle what is already on screen.
        val input = listOf(result("b", "Beta", 0.8f), result("a", "Alpha", 0.8f))

        val first = ranker.rank(query, input, UsageSnapshot.Empty).flatMap { it.results }.map { it.title }
        val second = ranker.rank(query, input.reversed(), UsageSnapshot.Empty).flatMap { it.results }.map { it.title }

        assertEquals(first, second)
        assertEquals(listOf("Alpha", "Beta"), first)
    }

    private companion object {
        const val NOW = 1_700_000_000_000L
    }
}
