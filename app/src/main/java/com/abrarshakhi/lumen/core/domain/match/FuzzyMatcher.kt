package com.abrarshakhi.lumen.core.domain.match

/**
 * Ranks how well a query matches a candidate string.
 *
 * A tiered, bonus-based matcher in the spirit of fzf, rather than edit distance over the
 * whole candidate list. Edit distance would rank `"Chrome"` and `"Chromecast"` almost
 * identically for the query `"chrome"`, and is far too slow to run per keystroke over
 * every installed app. Instead each tier below is both cheaper and more discriminating
 * than the next, and the first one that matches wins.
 *
 * Typo tolerance is deliberately the *last* resort: it is the only tier that can produce
 * surprising matches, so it only runs when every structural tier has failed.
 */
object FuzzyMatcher {

    private const val SCORE_EXACT = 1.0f
    private const val SCORE_PREFIX = 0.95f
    private const val SCORE_WORD_PREFIX = 0.90f
    private const val SCORE_ACRONYM = 0.85f
    private const val SCORE_SUBSTRING = 0.70f
    private const val SCORE_SUBSEQUENCE_MAX = 0.65f
    private const val SCORE_SUBSEQUENCE_MIN = 0.30f
    private const val SCORE_TYPO = 0.25f

    /**
     * Returns null when [query] does not match [target] at all.
     *
     * [query] is expected to be normalized already — callers hold a
     * [com.abrarshakhi.lumen.core.domain.search.SearchQuery] whose terms were normalized once.
     */
    fun score(query: String, target: SearchableText): MatchScore? {
        if (query.isEmpty()) return MatchScore(SCORE_SUBSEQUENCE_MIN, emptyList())
        val candidate = target.normalized
        if (candidate.isEmpty()) return null

        if (candidate == query) {
            return MatchScore(SCORE_EXACT, listOf(candidate.indices.toRange()))
        }
        if (candidate.startsWith(query)) {
            return MatchScore(SCORE_PREFIX, listOf(0 until query.length))
        }
        matchWordPrefix(query, target)?.let { return it }
        matchAcronym(query, target)?.let { return it }

        val substringAt = candidate.indexOf(query)
        if (substringAt >= 0) {
            // Later matches are weaker: "book" in "Facebook" should rank below "Booking".
            val positionPenalty = (substringAt.toFloat() / candidate.length) * 0.15f
            return MatchScore(SCORE_SUBSTRING - positionPenalty, listOf(substringAt until substringAt + query.length))
        }

        matchSubsequence(query, target)?.let { return it }
        return matchWithTypos(query, candidate)
    }

    /** `"maps"` matches `"Google Maps"` at a word boundary. */
    private fun matchWordPrefix(query: String, target: SearchableText): MatchScore? {
        val candidate = target.normalized
        for (start in target.wordStarts) {
            if (start == 0) continue
            if (candidate.startsWith(query, startIndex = start)) {
                val positionPenalty = (start.toFloat() / candidate.length) * 0.10f
                return MatchScore(SCORE_WORD_PREFIX - positionPenalty, listOf(start until start + query.length))
            }
        }
        return null
    }

    /** `"gm"` matches `"Google Maps"`; `"gps"` matches `"Google Play Store"`. */
    private fun matchAcronym(query: String, target: SearchableText): MatchScore? {
        if (query.length < 2 || target.acronym.length < 2) return null
        if (!target.acronym.startsWith(query)) return null
        val ranges = target.wordStarts.take(query.length).map { it..it }
        val completeness = query.length.toFloat() / target.acronym.length
        return MatchScore(SCORE_ACRONYM * (0.85f + 0.15f * completeness), ranges)
    }

    /**
     * Characters appear in order but not adjacently — `"gdocs"` matching `"Google Docs"`.
     *
     * Scored by how tightly packed the match is and how many matched characters landed on
     * word boundaries, so a compact, boundary-aligned match outranks a scattered one.
     */
    private fun matchSubsequence(query: String, target: SearchableText): MatchScore? {
        val candidate = target.normalized
        val wordStarts = target.wordStarts.toHashSet()
        val positions = ArrayList<Int>(query.length)

        var candidateIndex = 0
        for (queryChar in query) {
            var found = -1
            while (candidateIndex < candidate.length) {
                if (candidate[candidateIndex] == queryChar) {
                    found = candidateIndex
                    candidateIndex++
                    break
                }
                candidateIndex++
            }
            if (found < 0) return null
            positions += found
        }

        val span = positions.last() - positions.first() + 1
        val density = query.length.toFloat() / span
        val boundaryHits = positions.count { it in wordStarts }.toFloat() / query.length
        val headStart = 1f - (positions.first().toFloat() / candidate.length)

        val quality = (density * 0.5f) + (boundaryHits * 0.3f) + (headStart * 0.2f)
        val score = SCORE_SUBSEQUENCE_MIN + (SCORE_SUBSEQUENCE_MAX - SCORE_SUBSEQUENCE_MIN) * quality
        return MatchScore(score.coerceIn(SCORE_SUBSEQUENCE_MIN, SCORE_SUBSEQUENCE_MAX), positions.toRanges())
    }

    /**
     * Last resort: bounded Damerau-Levenshtein, so `"chrmoe"` still finds `"chrome"`.
     *
     * The allowance scales with query length and is capped — an unbounded distance would
     * match almost anything to almost anything.
     */
    private fun matchWithTypos(query: String, candidate: String): MatchScore? {
        val allowance = when {
            query.length <= 3 -> return null // too short to distinguish a typo from a different word
            query.length <= 5 -> 1
            else -> 2
        }

        val window = candidate.take(query.length + allowance)
        val distance = boundedDamerauLevenshtein(query, window, allowance)
        if (distance > allowance) return null

        val penalty = distance.toFloat() / (allowance + 1)
        return MatchScore(SCORE_TYPO * (1f - penalty * 0.5f), listOf(0 until minOf(query.length, candidate.length)))
    }

    /** Returns [limit] + 1 as soon as the distance is known to exceed [limit]. */
    private fun boundedDamerauLevenshtein(left: String, right: String, limit: Int): Int {
        if (kotlin.math.abs(left.length - right.length) > limit) return limit + 1

        var previousPrevious = IntArray(right.length + 1)
        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)

        for (i in 1..left.length) {
            current[0] = i
            var rowMinimum = current[0]
            for (j in 1..right.length) {
                val substitution = if (left[i - 1] == right[j - 1]) 0 else 1
                var cost = minOf(
                    current[j - 1] + 1,
                    previous[j] + 1,
                    previous[j - 1] + substitution,
                )
                val isTransposition = i > 1 && j > 1 &&
                    left[i - 1] == right[j - 2] && left[i - 2] == right[j - 1]
                if (isTransposition) {
                    cost = minOf(cost, previousPrevious[j - 2] + 1)
                }
                current[j] = cost
                if (cost < rowMinimum) rowMinimum = cost
            }
            if (rowMinimum > limit) return limit + 1

            val recycled = previousPrevious
            previousPrevious = previous
            previous = current
            current = recycled
        }
        return previous[right.length]
    }

    private fun IntRange.toRange(): IntRange = this

    /** Collapses sorted indices into contiguous ranges, so highlighting draws fewer spans. */
    private fun List<Int>.toRanges(): List<IntRange> {
        if (isEmpty()) return emptyList()
        val ranges = mutableListOf<IntRange>()
        var start = this[0]
        var end = this[0]
        for (index in drop(1)) {
            if (index == end + 1) {
                end = index
            } else {
                ranges += start..end
                start = index
                end = index
            }
        }
        ranges += start..end
        return ranges
    }
}
