package com.abrarshakhi.lumen.provider.web

/**
 * Works out what a raw query means to the web provider.
 *
 * Pure and free of Android, so every rule below is unit-testable — which matters because
 * these are the rules most likely to surprise someone: when a keyword applies, and when
 * something is a URL rather than a search.
 */
object WebQueryParser {

    private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://\\S+$")

    /**
     * A bare domain such as `github.com/foo`.
     *
     * The TLD must be at least two letters and the whole thing must contain no spaces, so
     * an ordinary sentence with a full stop is not mistaken for an address.
     */
    private val BARE_DOMAIN = Regex("^([\\w-]+\\.)+[a-zA-Z]{2,}(/\\S*)?$")

    sealed interface Parsed {
        /** Query routed to a specific engine by its keyword. */
        data class Engine(val engine: WebSearchEngine, val terms: String) : Parsed

        /** The query looks like an address and should be opened rather than searched. */
        data class Url(val url: String) : Parsed

        /** Ordinary query for the default engine. */
        data class Search(val terms: String) : Parsed

        /** Nothing actionable — e.g. a lone keyword with no query after it. */
        data object None : Parsed
    }

    fun parse(raw: String, engines: List<WebSearchEngine> = WebSearchEngine.BuiltIn): Parsed {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return Parsed.None

        val separator = trimmed.indexOfFirst { it.isWhitespace() }
        if (separator > 0) {
            val head = trimmed.substring(0, separator).lowercase()
            val engine = engines.firstOrNull { head in it.keywords }
            if (engine != null) {
                val terms = trimmed.substring(separator).trim()
                // "ggl" on its own is someone mid-typing, not a search for nothing.
                return if (terms.isEmpty()) Parsed.None else Parsed.Engine(engine, terms)
            }
        } else if (engines.any { trimmed.lowercase() in it.keywords }) {
            return Parsed.None
        }

        if (SCHEME.matches(trimmed)) return Parsed.Url(trimmed)
        if (BARE_DOMAIN.matches(trimmed)) return Parsed.Url("https://$trimmed")

        return Parsed.Search(trimmed)
    }
}
