package com.abrarshakhi.lumen.provider.settings

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.FuzzyMatcher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.repository.SystemSettingEntry
import com.abrarshakhi.lumen.core.domain.repository.SystemSettingsRepository
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.SnapshotSearchProvider
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Jumps straight to a system settings screen.
 *
 * Matches against each entry's title *and* its keywords, so "wifi" finds "Wi-Fi" and
 * "brightness" finds "Display" — the words people reach for are rarely the screen's name.
 */
class SystemSettingsProvider(
    private val settings: SystemSettingsRepository,
) : SnapshotSearchProvider() {

    override val id = ProviderId("system-settings")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_settings),
        category = ResultCategory.Setting,
        order = 60,
        timeout = 200.milliseconds,
        debounce = Duration.ZERO,
        minQueryLength = 2,
    )

    override suspend fun warmUp() {
        settings.entries()
    }

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val terms = query.normalizedTerms
        if (terms.isBlank()) return emptyList()

        return settings.entries()
            .mapNotNull { entry ->
                val match = entry.bestMatch(terms) ?: return@mapNotNull null
                entry.toResult(match)
            }
            .sortedByDescending { it.score }
            .take(MAX_RESULTS)
    }

    /**
     * Best score across the title and every keyword.
     *
     * Highlight ranges are kept only when the title itself matched — highlighting a title
     * using offsets computed against a keyword would underline the wrong characters.
     */
    private fun SystemSettingEntry.bestMatch(terms: String): Match? {
        val titleMatch = FuzzyMatcher.score(terms, searchable)
        val keywordBest = keywords
            .mapNotNull { FuzzyMatcher.score(terms, it) }
            .maxByOrNull { it.score }

        return when {
            titleMatch == null && keywordBest == null -> null
            titleMatch != null && (keywordBest == null || titleMatch.score >= keywordBest.score) ->
                Match(titleMatch.score, titleMatch.ranges)
            // A keyword hit is real but indirect, so it scores slightly below a title hit.
            else -> Match(keywordBest!!.score * KEYWORD_PENALTY, emptyList())
        }
    }

    private data class Match(val score: Float, val ranges: List<IntRange>)

    private fun SystemSettingEntry.toResult(match: Match) = SearchResult(
        id = ResultId("setting:$id"),
        providerId = this@SystemSettingsProvider.id,
        title = title,
        icon = IconSource.Vector(LumenIcon.Settings),
        category = ResultCategory.Setting,
        score = match.score,
        titleMatches = match.ranges,
        rankingKey = "setting:$id",
        actions = ResultActions(
            primary = ResultAction(
                id = "open-setting",
                label = TextValue.Res(R.string.action_open),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Launch(PlatformIntent.SystemSetting(action)) },
            ),
        ),
    )

    private companion object {
        const val MAX_RESULTS = 8
        const val KEYWORD_PENALTY = 0.9f
    }
}
