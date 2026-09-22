package com.abrarshakhi.lumen.core.platform.calendar

import android.content.Context
import android.provider.CalendarContract
import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.CalendarEvent
import com.abrarshakhi.lumen.core.domain.repository.CalendarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Calendar search over a window around today.
 *
 * Queries `Instances` rather than `Events`: the Instances table has recurrences already
 * expanded, so a weekly stand-up returns the *next* occurrence with real dates instead of
 * one row carrying an RRULE that would have to be interpreted here.
 */
class CalendarDataSource(
    private val context: Context,
    private val now: () -> Long = System::currentTimeMillis,
) : CalendarRepository {

    override suspend fun search(query: String, limit: Int): List<CalendarEvent> {
        val term = query.trim()
        if (term.length < MIN_TERM_LENGTH) return emptyList()

        return withContext(Dispatchers.IO) {
            runCatching { queryInstances(term, limit) }.getOrDefault(emptyList())
        }
    }

    private fun queryInstances(term: String, limit: Int): List<CalendarEvent> {
        val current = now()
        val from = current - TimeUnit.DAYS.toMillis(PAST_WINDOW_DAYS)
        val to = current + TimeUnit.DAYS.toMillis(FUTURE_WINDOW_DAYS)

        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(from.toString())
            .appendPath(to.toString())
            .build()

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.CALENDAR_DISPLAY_NAME,
        )

        // Filtered in the query so the provider does not pull every event across the window
        // over Binder just to discard most of them.
        val selection = "${CalendarContract.Instances.TITLE} LIKE ? ESCAPE '\\'"
        val selectionArgs = arrayOf("%${term.escapeForLike()}%")

        val results = mutableListOf<CalendarEvent>()

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${CalendarContract.Instances.BEGIN} ASC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val titleColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val beginColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val endColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val allDayColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.ALL_DAY)
            val locationColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            val descriptionColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            val calendarColumn = cursor.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_DISPLAY_NAME)

            while (cursor.moveToNext() && results.size < limit) {
                val title = cursor.getString(titleColumn)?.trim().orEmpty()
                if (title.isEmpty()) continue

                val location = cursor.getString(locationColumn)
                val description = cursor.getString(descriptionColumn)

                results += CalendarEvent(
                    eventId = cursor.getLong(idColumn),
                    title = title,
                    searchable = SearchableText.of(title),
                    beginMillis = cursor.getLong(beginColumn),
                    endMillis = cursor.getLong(endColumn),
                    isAllDay = cursor.getInt(allDayColumn) == 1,
                    location = location?.trim()?.takeIf { it.isNotEmpty() },
                    calendarName = cursor.getString(calendarColumn)?.trim()?.takeIf { it.isNotEmpty() },
                    meetingUrl = MeetingLinks.find(location, description),
                )
            }
        }

        return results
    }

    /** See the file-search data source: LIKE wildcards must be escaped, with ESCAPE declared. */
    private fun String.escapeForLike(): String =
        replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    private companion object {
        const val MIN_TERM_LENGTH = 2

        /** Recently-past events are still worth finding — "what was that meeting called?" */
        const val PAST_WINDOW_DAYS = 14L
        const val FUTURE_WINDOW_DAYS = 180L
    }
}
