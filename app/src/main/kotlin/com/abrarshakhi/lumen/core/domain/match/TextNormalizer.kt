package com.abrarshakhi.lumen.core.domain.match

import java.text.Normalizer

object TextNormalizer {

    private val COMBINING_MARKS = Regex("\\p{Mn}+")
    private val WHITESPACE = Regex("\\s+")

    fun normalize(input: String): String =
        Normalizer.normalize(input, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .lowercase()
            .replace(WHITESPACE, " ")
            .trim()

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

    fun acronym(normalized: String, wordStarts: IntArray): String =
        buildString(wordStarts.size) {
            for (index in wordStarts) {
                if (index < normalized.length) append(normalized[index])
            }
        }
}
