package com.abrarshakhi.lumen.core.domain.search

/**
 * A parsed user query.
 *
 * Parsing happens once, centrally, before providers are dispatched — so every provider sees
 * the same interpretation of a trigger word and none of them re-implements prefix parsing.
 *
 * For the raw input `"ggl kotlin flows"` with `ggl` registered as a trigger:
 * [raw] is the whole string, [trigger] is `"ggl"`, and [terms] is `"kotlin flows"`.
 */
data class SearchQuery(
    /** Exactly what the user typed, trimmed. */
    val raw: String,
    /** The recognised trigger keyword, if the query started with one. */
    val trigger: String? = null,
    /** The query with any trigger removed — what providers should actually match against. */
    val terms: String = raw,
    /** [terms] lowercased and diacritic-stripped, for matching. */
    val normalizedTerms: String = terms,
) {
    val isBlank: Boolean get() = terms.isBlank()
    val length: Int get() = terms.length
    val hasTrigger: Boolean get() = trigger != null

    companion object {
        val Empty = SearchQuery(raw = "", terms = "", normalizedTerms = "")
    }
}
