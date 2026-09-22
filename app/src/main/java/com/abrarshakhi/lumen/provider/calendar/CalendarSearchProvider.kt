package com.abrarshakhi.lumen.provider.calendar

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.FuzzyMatcher
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.repository.CalendarEvent
import com.abrarshakhi.lumen.core.domain.repository.CalendarEventFormatter
import com.abrarshakhi.lumen.core.domain.repository.CalendarRepository
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
import com.abrarshakhi.lumen.core.domain.search.TrailingContent
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Finds calendar events, and offers to join the call if there is one.
 *
 * The join action is the point of this provider: the common reason to search for a meeting
 * is to get into it, and the alternative is opening the calendar, finding the event, and
 * hunting for a link in the description.
 */
class CalendarSearchProvider(
    private val calendar: CalendarRepository,
    private val formatter: CalendarEventFormatter,
) : SnapshotSearchProvider() {

    override val id = ProviderId("calendar")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_calendar),
        category = ResultCategory.CalendarEvent,
        order = 45,
        requiredPermissions = listOf(AppPermission.ReadCalendar),
        timeout = 2.seconds,
        debounce = 120.milliseconds,
        minQueryLength = 2,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val events = calendar.search(query.terms, limit = MAX_RESULTS)
        val terms = query.normalizedTerms

        return events.map { event ->
            // The query already filtered by title; the matcher is only used to produce
            // highlight ranges and a relevance score for ordering against other providers.
            val match = FuzzyMatcher.score(terms, event.searchable)
            event.toResult(match?.score ?: BASE_SCORE, match?.ranges.orEmpty())
        }
    }

    private fun CalendarEvent.toResult(score: Float, matches: List<IntRange>) = SearchResult(
        // Keyed by occurrence, not by event: a recurring meeting has many instances and
        // they must not collapse into one row.
        id = ResultId("calendar:$eventId:$beginMillis"),
        providerId = this@CalendarSearchProvider.id,
        title = title,
        subtitle = listOfNotNull(formatter.describe(this), location, calendarName)
            .joinToString(" · ")
            .takeIf { it.isNotBlank() },
        icon = IconSource.Vector(LumenIcon.Calendar),
        category = ResultCategory.CalendarEvent,
        score = score,
        titleMatches = matches,
        rankingKey = "calendar:$eventId",
        trailing = meetingUrl?.let { TrailingContent.Badge(TextValue.Res(R.string.calendar_call)) },
        actions = ResultActions(
            // When there is a call, joining it is what the user came for.
            primary = if (meetingUrl != null) {
                ResultAction(
                    id = "join-call",
                    label = TextValue.Res(R.string.calendar_join),
                    icon = IconSource.Vector(LumenIcon.Video),
                    kind = ActionKind.Open,
                    invoke = { ActionOutcome.Launch(PlatformIntent.ViewUri(meetingUrl)) },
                )
            } else {
                openAction()
            },
            secondary = buildList {
                if (meetingUrl != null) add(openAction())
            },
        ),
    )

    private fun CalendarEvent.openAction() = ResultAction(
        id = "open-event",
        label = TextValue.Res(R.string.action_open),
        icon = IconSource.Vector(LumenIcon.Calendar),
        kind = ActionKind.Open,
        invoke = {
            ActionOutcome.Launch(PlatformIntent.CalendarEvent(eventId, beginMillis, endMillis))
        },
    )

    private companion object {
        const val MAX_RESULTS = 8

        /** Used when the matcher declines but the calendar query already matched the title. */
        const val BASE_SCORE = 0.7f
    }
}
