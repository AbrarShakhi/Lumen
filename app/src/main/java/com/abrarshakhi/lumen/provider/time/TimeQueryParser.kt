package com.abrarshakhi.lumen.provider.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Recognises the two time questions worth answering inline.
 *
 * Deliberately a small, explicit grammar rather than general natural-language parsing:
 * a launcher that sometimes guesses wrong about what you meant is worse than one that only
 * answers what it is sure about, and every rule here is testable.
 */
object TimeQueryParser {

    sealed interface Parsed {
        /** "time in tokyo" — the current time somewhere else. */
        data class Clock(val place: WorldClock.Place) : Parsed

        /** "today + 3 weeks" — a date offset from a known anchor. */
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
        // Only one of the two alternatives captures, so take whichever is present.
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
