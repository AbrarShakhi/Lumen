package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

/** One occurrence of a calendar event. */
data class CalendarEvent(
    val eventId: Long,
    val title: String,
    val searchable: SearchableText,
    val beginMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
    val location: String?,
    val calendarName: String?,
    /** A meeting link found in the location or description, if any. */
    val meetingUrl: String?,
)

/**
 * Searches calendar events.
 *
 * Queries a bounded window around today rather than the whole calendar: an event from three
 * years ago is almost never what someone is looking for, and expanding every recurrence
 * over an unbounded range is expensive.
 */
interface CalendarRepository {
    suspend fun search(query: String, limit: Int = 10): List<CalendarEvent>
}
