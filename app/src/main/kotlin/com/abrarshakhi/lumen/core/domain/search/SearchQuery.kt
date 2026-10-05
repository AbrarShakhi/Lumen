package com.abrarshakhi.lumen.core.domain.search

data class SearchQuery(
    val raw: String,
    val trigger: String? = null,
    val terms: String = raw,
    val normalizedTerms: String = terms,
) {
    val isBlank: Boolean get() = terms.isBlank()
    val length: Int get() = terms.length
    val hasTrigger: Boolean get() = trigger != null

    companion object {
        val Empty = SearchQuery(raw = "", terms = "", normalizedTerms = "")
    }
}
