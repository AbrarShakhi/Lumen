package com.abrarshakhi.lumen.core.domain.repository

interface CalendarEventFormatter {
    fun describe(event: CalendarEvent): String
}
