package com.abrarshakhi.lumen.core.domain.repository

/**
 * Renders an event's timing for display.
 *
 * Lives in the domain because both sides need it and neither may depend on the other: the
 * calendar provider consumes it, the platform layer implements it with Android's date
 * utilities. Declaring it in the provider package would have made `core/platform` depend on
 * `provider/`, which is backwards.
 */
interface CalendarEventFormatter {
    fun describe(event: CalendarEvent): String
}
