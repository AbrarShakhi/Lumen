package com.abrarshakhi.lumen.core.domain.rank

import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.SearchResult

data class ResultSection(
    val category: ResultCategory,
    val results: List<SearchResult>,
    val order: Int,
    val hasMore: Boolean = false,
)
