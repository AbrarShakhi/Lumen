package com.abrarshakhi.lumen.core.domain.match

/**
 * A pre-processed string ready to be matched against, with the derived forms computed once.
 *
 * Built at *index* time, never per keystroke: normalisation and word-boundary detection
 * over a few hundred app labels is wasted work if repeated on every character typed.
 */
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

        /** Rebuilds from values already persisted, skipping recomputation. */
        fun restored(original: String, normalized: String, acronym: String): SearchableText =
            SearchableText(
                original = original,
                normalized = normalized,
                wordStarts = TextNormalizer.wordStarts(original, normalized),
                acronym = acronym,
            )
    }
}

/**
 * How well a query matched, and where.
 *
 * [ranges] index into [SearchableText.normalized], which is index-aligned with the original
 * for every case Lumen handles, so the UI can highlight the matched characters directly.
 */
data class MatchScore(val score: Float, val ranges: List<IntRange>)
