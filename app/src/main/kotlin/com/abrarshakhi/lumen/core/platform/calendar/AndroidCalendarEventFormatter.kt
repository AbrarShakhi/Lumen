package com.abrarshakhi.lumen.core.platform.calendar

import android.content.Context
import android.text.format.DateUtils
import com.abrarshakhi.lumen.core.domain.repository.CalendarEvent
import com.abrarshakhi.lumen.core.domain.repository.CalendarEventFormatter

class AndroidCalendarEventFormatter(
    private val context: Context,
) : CalendarEventFormatter {

    override fun describe(event: CalendarEvent): String {
        val flags = if (event.isAllDay) {
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
