package com.abrarshakhi.lumen.core.domain.rank

import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.SearchResult

/**
 * A group of results sharing a category, in display order.
 *
 * Carries no title: the heading is presentation, and localising it here would mean the
 * domain layer reaching for string resources. The UI maps [category] to a label.
 */
data class ResultSection(
    val category: ResultCategory,
    val results: List<SearchResult>,
    /** Section ordering, derived from category rather than DI registration order. */
    val order: Int,
    /** True when results were trimmed by the per-section cap. */
    val hasMore: Boolean = false,
)
