package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.text.TextValue

/**
 * One row in the result list.
 *
 * Provider-specific richness travels in [trailing] and [expanded] rather than in the
 * screen's state. That is what keeps the search ViewModel from growing a branch per
 * provider: it renders results, and results carry their own presentation.
 *
 * Never compare these structurally — [actions] holds lambdas, so `equals` is meaningless.
 * Identity is [id].
 */
data class SearchResult(
    val id: ResultId,
    val providerId: ProviderId,
    /** Always data (an app label, a contact name), never a localised string. */
    val title: String,
    val subtitle: String? = null,
    val icon: IconSource,
    val category: ResultCategory,
    /** Provider-local relevance in `0f..1f`. The ranker combines this with usage data. */
    val score: Float,
    /** Character ranges in [title] that matched the query, for highlighting. */
    val titleMatches: List<IntRange> = emptyList(),
    val actions: ResultActions,
    /** Compact content at the row's end — a computed value, a badge, a count. */
    val trailing: TrailingContent? = null,
    /** Content shown below the row when it is the focused result. */
    val expanded: ExpandedContent? = null,
    /**
     * The key under which this result's launches are counted for usage ranking.
     *
     * Defaults to [id]. Providers whose ids vary by query (a web search, say) should pin
     * a stable key here so usage actually accumulates.
     */
    val rankingKey: String = id.value,
    /** Whether the user may assign a trigger keyword to this result. */
    val triggerable: Boolean = true,
)

/** Compact end-of-row content. */
sealed interface TrailingContent {
    data class Text(val value: String) : TrailingContent
    data class Badge(val label: TextValue) : TrailingContent
    data class Toggle(val checked: Boolean) : TrailingContent
    data object Progress : TrailingContent
}

/** Content revealed beneath a focused result. */
sealed interface ExpandedContent {
    /** Body text, e.g. a streaming AI answer or a conversion breakdown. */
    data class Body(val text: String, val isStreaming: Boolean = false) : ExpandedContent
    data class KeyValues(val entries: List<Pair<String, String>>) : ExpandedContent
}
