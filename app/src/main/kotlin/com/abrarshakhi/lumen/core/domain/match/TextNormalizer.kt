package com.abrarshakhi.lumen.core.domain.match

import java.text.Normalizer

/**
 * Folds text into a canonical form for matching: lowercase, diacritic-free, and with
 * runs of whitespace collapsed.
 *
 * Uses `java.text.Normalizer`, which is JDK rather than Android API, so this stays
 * unit-testable on the JVM without Robolectric.
 */
object TextNormalizer {

    private val COMBINING_MARKS = Regex("\\p{Mn}+")
    private val WHITESPACE = Regex("\\s+")

    /** `"Señor  Café"` becomes `"senor cafe"`. */
    fun normalize(input: String): String =
        Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .lowercase()
            .replace(WHITESPACE, " ")
            .trim()

    /**
     * Indices in [normalized] at which a word begins.
     *
     * A word starts after whitespace or a separator, and also at a lower-to-upper case
     * transition in the *original* text, so `"YouTube"` yields starts at `Y` and `T` and
     * an acronym of `"yt"`.
     */
    fun wordStarts(original: String, normalized: String): IntArray {
        if (normalized.isEmpty()) return IntArray(0)
        val starts = mutableListOf(0)
        for (i in 1 until normalized.length) {
            val previous = normalized[i - 1]
            val isBoundary = !previous.isLetterOrDigit()
            val isCamelHump = i < original.length &&
                original[i].isUpperCase() &&
                original.getOrNull(i - 1)?.isLowerCase() == true
            if ((isBoundary || isCamelHump) && normalized[i].isLetterOrDigit()) {
                starts += i
            }
        }
        return starts.distinct().toIntArray()
    }

    /** The initial of each word — `"Google Play Store"` becomes `"gps"`. */
    fun acronym(normalized: String, wordStarts: IntArray): String =
        buildString(wordStarts.size) {
            for (index in wordStarts) {
                if (index < normalized.length) append(normalized[index])
            }
        }
}
