package com.abrarshakhi.lumen.provider.web

import com.abrarshakhi.lumen.core.domain.search.LumenIcon

data class WebSearchEngine(
    val id: String,
    val displayName: String,
    val keywords: Set<String>,
    val queryTemplate: String,
    val icon: LumenIcon = LumenIcon.Web,
) {
    fun urlFor(encodedQuery: String): String = queryTemplate.replace("%s", encodedQuery)

    companion object {
        val BuiltIn: List<WebSearchEngine> = listOf(
            WebSearchEngine(
                id = "google",
                displayName = "Google",
                keywords = setOf("g", "ggl", "google"),
                queryTemplate = "https://www.google.com/search?q=%s",
            ),
            WebSearchEngine(
                id = "duckduckgo",
                displayName = "DuckDuckGo",
                keywords = setOf("ddg", "duck"),
                queryTemplate = "https://duckduckgo.com/?q=%s",
            ),
            WebSearchEngine(
                id = "youtube",
                displayName = "YouTube",
                keywords = setOf("ytb", "youtube"),
                queryTemplate = "https://www.youtube.com/results?search_query=%s",
                icon = LumenIcon.Video,
            ),
            WebSearchEngine(
                id = "wikipedia",
                displayName = "Wikipedia",
                keywords = setOf("w", "wiki"),
                queryTemplate = "https://en.wikipedia.org/w/index.php?search=%s",
            ),
            WebSearchEngine(
                id = "github",
                displayName = "GitHub",
                keywords = setOf("gh", "github"),
                queryTemplate = "https://github.com/search?q=%s",
            ),
            WebSearchEngine(
                id = "maps",
                displayName = "Maps",
                keywords = setOf("map", "maps"),
                queryTemplate = "https://www.google.com/maps/search/%s",
            ),
        )

        val Default: WebSearchEngine = BuiltIn.first()
    }
}
