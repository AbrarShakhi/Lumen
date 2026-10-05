package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.match.TextNormalizer

interface TriggerResolver {
    fun parse(raw: String): SearchQuery
}

fun interface TriggerSource {
    fun triggers(): Map<String, String>
}

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
