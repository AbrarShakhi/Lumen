package com.abrarshakhi.lumen.provider.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object TimeQueryParser {

    sealed interface Parsed {
        data class Clock(val place: WorldClock.Place) : Parsed

        data class DateOffset(val base: LocalDate, val amount: Long, val unit: ChronoUnit) : Parsed
    }

    private val CLOCK = Regex(
        """^\s*(?:time|clock)\s+(?:in|at)\s+(.+?)\s*$|^\s*(.+?)\s+time\s*$""",
        RegexOption.IGNORE_CASE,
    )

    private val DATE_OFFSET = Regex(
        """^\s*(today|now|tomorrow|yesterday)\s*([-+])\s*(\d+)\s*(day|days|week|weeks|month|months|year|years)\s*$""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(input: String, today: LocalDate = LocalDate.now()): Parsed? {
        parseDateOffset(input, today)?.let { return it }
        return parseClock(input)
    }

    private fun parseClock(input: String): Parsed.Clock? {
        val match = CLOCK.find(input) ?: return null
        val name = match.groupValues.drop(1).firstOrNull { it.isNotBlank() } ?: return null
        val place = WorldClock.find(name) ?: return null
        return Parsed.Clock(place)
    }

    private fun parseDateOffset(input: String, today: LocalDate): Parsed.DateOffset? {
        val match = DATE_OFFSET.find(input) ?: return null
        val (anchor, sign, rawAmount, rawUnit) = match.destructured

        val base = when (anchor.lowercase()) {
            "today", "now" -> today
            "tomorrow" -> today.plusDays(1)
            "yesterday" -> today.minusDays(1)
            else -> return null
        }

        val amount = rawAmount.toLongOrNull() ?: return null
        val signed = if (sign == "-") -amount else amount

        val unit = when (rawUnit.lowercase().removeSuffix("s")) {
            "day" -> ChronoUnit.DAYS
            "week" -> ChronoUnit.WEEKS
            "month" -> ChronoUnit.MONTHS
            "year" -> ChronoUnit.YEARS
            else -> return null
        }

        return Parsed.DateOffset(base, signed, unit)
    }
}
