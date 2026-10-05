package com.abrarshakhi.lumen.core.domain.match

class SearchableText private constructor(
    val original: String,
    val normalized: String,
    val wordStarts: IntArray,
    val acronym: String,
) {
    companion object {
        fun of(original: String): SearchableText {
            val normalized = TextNormalizer.normalize(original)
            val wordStarts = TextNormalizer.wordStarts(original, normalized)
            return SearchableText(
                original = original,
                normalized = normalized,
                wordStarts = wordStarts,
                acronym = TextNormalizer.acronym(normalized, wordStarts),
            )
        }

        fun restored(original: String, normalized: String, acronym: String): SearchableText =
            SearchableText(
                original = original,
                normalized = normalized,
                wordStarts = TextNormalizer.wordStarts(original, normalized),
                acronym = acronym,
            )
    }
}

data class MatchScore(val score: Float, val ranges: List<IntRange>)
