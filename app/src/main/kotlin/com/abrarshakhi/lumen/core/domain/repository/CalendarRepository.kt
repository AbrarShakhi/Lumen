package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

data class CalendarEvent(
    val eventId: Long,
    val title: String,
    val searchable: SearchableText,
    val beginMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean,
    val location: String?,
    val calendarName: String?,
    val meetingUrl: String?,
)

interface CalendarRepository {
    suspend fun search(query: String, limit: Int = 10): List<CalendarEvent>
}
