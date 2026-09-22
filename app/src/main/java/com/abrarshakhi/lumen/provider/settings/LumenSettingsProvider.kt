package com.abrarshakhi.lumen.provider.settings

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.FuzzyMatcher
import com.abrarshakhi.lumen.core.domain.repository.LumenSettingEntry
import com.abrarshakhi.lumen.core.domain.repository.LumenSettingsRepository
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
 * Makes Lumen's own settings reachable from the search field.
 *
 * Uses [ActionOutcome.Navigate], so the destination is expressed as a domain concept and
 * mapped to a route in the app layer — the provider never imports a navigation type.
 */
class LumenSettingsProvider(
    private val settings: LumenSettingsRepository,
) : SnapshotSearchProvider() {

    override val id = ProviderId("lumen-settings")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_lumen),
        category = ResultCategory.Setting,
        order = 61,
        timeout = 100.milliseconds,
        debounce = Duration.ZERO,
        minQueryLength = 3,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val terms = query.normalizedTerms
        if (terms.isBlank()) return emptyList()

        return settings.entries().mapNotNull { entry ->
            val best = entry.matchAgainst(terms) ?: return@mapNotNull null

            SearchResult(
                id = ResultId("lumen:${entry.id}"),
                providerId = id,
                title = entry.title,
                icon = IconSource.Vector(LumenIcon.Settings),
                category = ResultCategory.Setting,
                score = best,
                rankingKey = "lumen:${entry.id}",
                actions = ResultActions(
                    primary = ResultAction(
                        id = "open",
                        label = TextValue.Res(R.string.lumen_setting_hint),
                        icon = IconSource.Vector(LumenIcon.Open),
                        kind = ActionKind.Open,
                        invoke = { ActionOutcome.Navigate(entry.destination) },
                    ),
                ),
            )
        }.sortedByDescending { it.score }
    }

    /** Best score across the title and every keyword alias. */
    private fun LumenSettingEntry.matchAgainst(terms: String): Float? =
        (listOf(searchable) + keywords)
            .mapNotNull { FuzzyMatcher.score(terms, it) }
            .maxOfOrNull { it.score }
}
