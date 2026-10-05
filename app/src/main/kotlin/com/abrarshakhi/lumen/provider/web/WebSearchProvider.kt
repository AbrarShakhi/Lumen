package com.abrarshakhi.lumen.provider.web

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.SnapshotSearchProvider
import com.abrarshakhi.lumen.core.domain.text.TextValue
import java.net.URLEncoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Always-available fallback: search the web, open a typed address, or route to a specific
 * engine by keyword.
 *
 * Ranked last by [ResultCategory.WebSearch]'s weight, on purpose — the web is always able
 * to answer, so if it competed on score it would crowd out local results that are almost
 * always what the user actually meant.
 */
class WebSearchProvider(
    private val engines: List<WebSearchEngine> = WebSearchEngine.BuiltIn,
    private val defaultEngine: WebSearchEngine = WebSearchEngine.Default,
) : SnapshotSearchProvider() {

    override val id = ProviderId("web")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_web),
        category = ResultCategory.WebSearch,
        order = 90,
        // Purely local string work — no network, so nothing to debounce or time out for.
        timeout = 100.milliseconds,
        debounce = Duration.ZERO,
        minQueryLength = 2,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> =
        when (val parsed = WebQueryParser.parse(query.terms, engines)) {
            WebQueryParser.Parsed.None -> emptyList()

            is WebQueryParser.Parsed.Url -> listOf(openUrlResult(parsed.url))

            // An explicit keyword is an instruction, so it outscores the generic fallback
            // and is the only web result offered.
            is WebQueryParser.Parsed.Engine ->
                listOf(searchResult(parsed.engine, parsed.terms, score = 1f))

            is WebQueryParser.Parsed.Search ->
                listOf(searchResult(defaultEngine, parsed.terms, score = 0.4f))
        }

    private fun searchResult(engine: WebSearchEngine, terms: String, score: Float): SearchResult {
        val url = engine.urlFor(URLEncoder.encode(terms, Charsets.UTF_8.name()))
        return SearchResult(
            id = ResultId("web:${engine.id}:$terms"),
            providerId = id,
            title = terms,
            subtitle = engine.displayName,
            icon = IconSource.Vector(engine.icon),
            category = ResultCategory.WebSearch,
            score = score,
            // Usage accrues per engine rather than per query, or every distinct search
            // would be its own never-repeated ranking key and learn nothing.
            rankingKey = "web:${engine.id}",
            actions = ResultActions(
                primary = ResultAction(
                    id = ACTION_SEARCH,
                    label = TextValue.Res(R.string.action_search_web),
                    icon = IconSource.Vector(LumenIcon.Web),
                    kind = ActionKind.Open,
                    invoke = { ActionOutcome.Launch(PlatformIntent.ViewUri(url)) },
                ),
                secondary = listOf(
                    ResultAction(
                        id = ACTION_COPY,
                        label = TextValue.Res(R.string.action_copy),
                        icon = IconSource.Vector(LumenIcon.Copy),
                        kind = ActionKind.Copy,
                        invoke = { context ->
                            context.copyToClipboard(engine.displayName, url)
                            ActionOutcome.Message(TextValue.Res(R.string.copied))
                        },
                    ),
                ),
            ),
        )
    }

    private fun openUrlResult(url: String) = SearchResult(
        id = ResultId("web:url:$url"),
        providerId = id,
        title = url,
        subtitle = null,
        icon = IconSource.Vector(LumenIcon.Web),
        category = ResultCategory.WebSearch,
        // A typed address is unambiguous, so it beats searching for its text.
        score = 1f,
        rankingKey = "web:url",
        actions = ResultActions(
            primary = ResultAction(
                id = ACTION_OPEN,
                label = TextValue.Res(R.string.action_open_link),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Launch(PlatformIntent.ViewUri(url)) },
            ),
        ),
    )

    private companion object {
        const val ACTION_SEARCH = "search-web"
        const val ACTION_OPEN = "open-link"
        const val ACTION_COPY = "copy-link"
    }
}
