package com.abrarshakhi.lumen.feature.search

import com.abrarshakhi.lumen.core.domain.rank.ResultSection
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The reducer is a pure function, which is exactly why these tests need no dispatcher,
 * no Turbine and no coroutine machinery — the payoff for splitting Intent from Action.
 */
class SearchReducerTest {

    private fun result(id: String, title: String = id) = SearchResult(
        id = ResultId(id),
        providerId = ProviderId("apps"),
        title = title,
        icon = IconSource.Vector(LumenIcon.App),
        category = ResultCategory.App,
        score = 1f,
        actions = ResultActions(
            primary = ResultAction(
                id = "open",
                label = TextValue.Raw("Open"),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Handled },
            ),
        ),
    )

    private fun section(vararg results: SearchResult) =
        ResultSection(ResultCategory.App, results.toList(), order = 10)

    @Test
    fun `query echo updates only the query`() {
        val before = SearchState(sections = listOf(section(result("a"))))

        val after = SearchReducer.reduce(before, SearchAction.QueryEchoed("nu"))

        assertEquals("nu", after.query)
        assertEquals(before.sections, after.sections, "echoing must not clear existing results")
    }

    @Test
    fun `results update replaces sections and pending providers`() {
        val after = SearchReducer.reduce(
            SearchState(),
            SearchAction.ResultsUpdated(
                query = "yt",
                sections = listOf(section(result("a", "YouTube"))),
                pendingProviders = setOf(ProviderId("web")),
                permissionRequests = emptyList(),
            ),
        )

        assertEquals("yt", after.query)
        assertEquals(listOf("YouTube"), after.sections.flatMap { it.results }.map { it.title })
        assertTrue(after.isSearching, "a pending provider means search is still in progress")
    }

    @Test
    fun `sheet opens and closes`() {
        val target = result("a")

        val opened = SearchReducer.reduce(SearchState(), SearchAction.SheetOpened(target))
        assertEquals(target, opened.sheet?.result)

        val closed = SearchReducer.reduce(opened, SearchAction.SheetClosed)
        assertNull(closed.sheet)
    }

    @Test
    fun `reset clears everything`() {
        val populated = SearchState(
            query = "yt",
            sections = listOf(section(result("a"))),
            pendingProviders = setOf(ProviderId("apps")),
            sheet = ResultSheet(result("a")),
        )

        assertEquals(SearchState(), SearchReducer.reduce(populated, SearchAction.Reset))
    }

    @Test
    fun `top result is the first of the first section`() {
        val state = SearchState(
            sections = listOf(
                ResultSection(ResultCategory.Answer, listOf(result("ans", "42")), order = -50),
                section(result("app", "YouTube")),
            ),
        )

        assertEquals("42", state.topResult?.title, "the IME action key must activate the top-ranked result")
    }

    @Test
    fun `results can be found by id across sections`() {
        val state = SearchState(
            sections = listOf(
                ResultSection(ResultCategory.Answer, listOf(result("ans")), order = -50),
                section(result("app")),
            ),
        )

        assertEquals("app", state.resultById(ResultId("app"))?.id?.value)
        assertNull(state.resultById(ResultId("missing")))
    }
}
