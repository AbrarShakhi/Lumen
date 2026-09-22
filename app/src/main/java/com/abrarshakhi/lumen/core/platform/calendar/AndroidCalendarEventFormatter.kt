package com.abrarshakhi.lumen.core.platform.calendar

import android.content.Context
import android.text.format.DateUtils
import com.abrarshakhi.lumen.core.domain.repository.CalendarEvent
import com.abrarshakhi.lumen.core.domain.repository.CalendarEventFormatter

/**
 * Formats event timing with the platform's own date utilities.
 *
 * `DateUtils` is used rather than a hand-rolled formatter because it already handles the
 * things that are easy to get wrong: the user's 12/24-hour setting, locale date order, and
 * saying "Today"/"Tomorrow" where that is clearer than a date.
 */
class AndroidCalendarEventFormatter(
    private val context: Context,
) : CalendarEventFormatter {

    override fun describe(event: CalendarEvent): String {
        val flags = if (event.isAllDay) {
            // An all-day event's end is exclusive midnight, so showing its time would read
            // as though it ran until the small hours of the next day.
            DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_WEEKDAY or
                DateUtils.FORMAT_ABBREV_ALL or
                DateUtils.FORMAT_UTC
        } else {
            DateUtils.FORMAT_SHOW_TIME or
                DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_ABBREV_ALL
        }

        return DateUtils.formatDateRange(
            context,
            event.beginMillis,
            if (event.isAllDay) event.beginMillis else event.endMillis,
            flags,
        )
    }
}
