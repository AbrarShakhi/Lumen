package com.abrarshakhi.lumen.core.domain.rank

import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult

/**
 * Orders and groups results for display.
 *
 * Pure and synchronous by design: everything it needs is already in memory, which makes
 * the whole ranking policy testable with plain `assertEquals` and keeps it off the I/O path.
 */
interface ResultRanker {
    fun rank(
        query: SearchQuery,
        results: List<SearchResult>,
        usage: UsageSnapshot,
    ): List<ResultSection>
}

/** Tunable weights for [DefaultResultRanker]. */
data class RankingWeights(
    val relevance: Float = 1.0f,
    val usage: Float = 0.45f,
    /** Results per section before "show more". */
    val sectionCaps: Map<ResultCategory, Int> = DEFAULT_CAPS,
) {
    companion object {
        val DEFAULT_CAPS: Map<ResultCategory, Int> = mapOf(
            ResultCategory.Answer to 3,
            ResultCategory.App to 5,
            ResultCategory.Shortcut to 3,
            ResultCategory.Contact to 3,
            ResultCategory.Note to 3,
            ResultCategory.File to 4,
            ResultCategory.CalendarEvent to 3,
            ResultCategory.Setting to 3,
            ResultCategory.WebSearch to 4,
            ResultCategory.System to 2,
        )
        val Default = RankingWeights()
    }
}

class DefaultResultRanker(
    private val weights: RankingWeights = RankingWeights.Default,
) : ResultRanker {

    override fun rank(
        query: SearchQuery,
        results: List<SearchResult>,
        usage: UsageSnapshot,
    ): List<ResultSection> {
        if (results.isEmpty()) return emptyList()

        val scored = results
            .distinctBy { it.id }
            .map { result -> result to finalScore(result, usage) }
            .sortedWith(
                compareByDescending<Pair<SearchResult, Float>> { it.second }
                    // Ties must not reorder between emissions, or the list flickers as
                    // slower providers arrive. Title then id give a total order.
                    .thenBy { it.first.title.lowercase() }
                    .thenBy { it.first.id.value },
            )

        return scored
            .groupBy { it.first.category }
            .map { (category, entries) ->
                val cap = weights.sectionCaps[category] ?: DEFAULT_CAP
                ResultSection(
                    category = category,
                    results = entries.take(cap).map { it.first },
                    order = category.displayOrder,
                    hasMore = entries.size > cap,
                )
            }
            .sortedBy { it.order }
    }

    private fun finalScore(result: SearchResult, usage: UsageSnapshot): Float {
        val relevance = result.score * result.category.weight * weights.relevance
        val familiarity = usage.scoreFor(result.rankingKey) * weights.usage
        return relevance + familiarity
    }


    private companion object {
        const val DEFAULT_CAP = 3
    }
}
