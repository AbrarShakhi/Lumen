package com.abrarshakhi.lumen.provider.web

object WebQueryParser {

    private val SCHEME = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*://\\S+$")

    private val BARE_DOMAIN = Regex("^([\\w-]+\\.)+[a-zA-Z]{2,}(/\\S*)?$")

    sealed interface Parsed {
        data class Engine(val engine: WebSearchEngine, val terms: String) : Parsed

        data class Url(val url: String) : Parsed

        data class Search(val terms: String) : Parsed

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
