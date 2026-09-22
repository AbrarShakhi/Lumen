package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.match.TextNormalizer

/**
 * Turns raw input into a [SearchQuery], recognising trigger keywords.
 *
 * Parsing happens once, centrally, before dispatch — so `ggl kotlin` means the same thing
 * to every provider and none of them re-implements prefix handling.
 */
interface TriggerResolver {
    fun parse(raw: String): SearchQuery
}

/** Supplies the currently registered trigger keywords. Backed by persistence at runtime. */
fun interface TriggerSource {
    /** Lowercased keyword to the id of the result it opens. */
    fun triggers(): Map<String, String>
}

/**
 * Recognises a trigger only when it is the first whitespace-delimited word.
 *
 * Requiring the separating space is what stops a keyword like `s` from hijacking every
 * query that happens to begin with that letter.
 */
class DefaultTriggerResolver(
    private val source: TriggerSource = TriggerSource { emptyMap() },
) : TriggerResolver {

    override fun parse(raw: String): SearchQuery {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return SearchQuery.Empty

        val separator = trimmed.indexOfFirst { it.isWhitespace() }
        if (separator > 0) {
            val candidate = trimmed.substring(0, separator).lowercase()
            if (source.triggers().containsKey(candidate)) {
                val remainder = trimmed.substring(separator).trim()
                return SearchQuery(
                    raw = trimmed,
                    trigger = candidate,
                    terms = remainder,
                    normalizedTerms = TextNormalizer.normalize(remainder),
                )
            }
        }

        return SearchQuery(
            raw = trimmed,
            trigger = null,
            terms = trimmed,
            normalizedTerms = TextNormalizer.normalize(trimmed),
        )
    }
}
