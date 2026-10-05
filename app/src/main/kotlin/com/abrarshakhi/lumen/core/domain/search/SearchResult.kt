package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.text.TextValue

data class SearchResult(
    val id: ResultId,
    val providerId: ProviderId,
    val title: String,
    val subtitle: String? = null,
    val icon: IconSource,
    val category: ResultCategory,
    val score: Float,
    val titleMatches: List<IntRange> = emptyList(),
    val actions: ResultActions,
    val trailing: TrailingContent? = null,
    val expanded: ExpandedContent? = null,
    val rankingKey: String = id.value,
    val triggerable: Boolean = true,
)

sealed interface TrailingContent {
    data class Text(val value: String) : TrailingContent
    data class Badge(val label: TextValue) : TrailingContent
    data class Toggle(val checked: Boolean) : TrailingContent
    data object Progress : TrailingContent
}

sealed interface ExpandedContent {
    data class Body(val text: String, val isStreaming: Boolean = false) : ExpandedContent
    data class KeyValues(val entries: List<Pair<String, String>>) : ExpandedContent
}
