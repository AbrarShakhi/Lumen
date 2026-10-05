package com.abrarshakhi.lumen.provider.time

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TimeQueryParserTest {

    private val today = LocalDate.of(2026, 3, 15)

    @Test
    fun `time in a city resolves its zone`() {
        val parsed = assertIs<TimeQueryParser.Parsed.Clock>(TimeQueryParser.parse("time in tokyo", today))
        assertEquals("Asia/Tokyo", parsed.place.zone.id)
    }

    @Test
    fun `the trailing form works too`() {
        val parsed = assertIs<TimeQueryParser.Parsed.Clock>(TimeQueryParser.parse("london time", today))
        assertEquals("Europe/London", parsed.place.zone.id)
    }

    @Test
    fun `colloquial aliases resolve`() {
        assertEquals(
            "America/New_York",
            assertIs<TimeQueryParser.Parsed.Clock>(TimeQueryParser.parse("time in nyc", today)).place.zone.id,
        )
        assertEquals(
            "Asia/Dhaka",
            assertIs<TimeQueryParser.Parsed.Clock>(TimeQueryParser.parse("time in bangladesh", today)).place.zone.id,
        )
    }

    @Test
    fun `city names are matched case insensitively`() {
        assertNotNull(TimeQueryParser.parse("time in TOKYO", today))
        assertNotNull(TimeQueryParser.parse("Time In Paris", today))
    }

    @Test
    fun `multi-word cities resolve`() {
        assertEquals(
            "America/Los_Angeles",
            assertIs<TimeQueryParser.Parsed.Clock>(
                TimeQueryParser.parse("time in los angeles", today),
            ).place.zone.id,
        )
    }

    @Test
    fun `date offsets are computed from the right anchor`() {
        val parsed = assertIs<TimeQueryParser.Parsed.DateOffset>(
            TimeQueryParser.parse("today + 3 days", today),
        )
        assertEquals(today, parsed.base)
        assertEquals(3L, parsed.amount)
        assertEquals(ChronoUnit.DAYS, parsed.unit)
        assertEquals(LocalDate.of(2026, 3, 18), parsed.base.plus(parsed.amount, parsed.unit))
    }

    @Test
    fun `subtraction and other anchors work`() {
        val back = assertIs<TimeQueryParser.Parsed.DateOffset>(
            TimeQueryParser.parse("today - 2 weeks", today),
        )
        assertEquals(LocalDate.of(2026, 3, 1), back.base.plus(back.amount, back.unit))

        val fromTomorrow = assertIs<TimeQueryParser.Parsed.DateOffset>(
            TimeQueryParser.parse("tomorrow + 1 month", today),
        )
        assertEquals(LocalDate.of(2026, 4, 16), fromTomorrow.base.plus(fromTomorrow.amount, fromTomorrow.unit))
    }

    @Test
    fun `unknown places yield nothing`() {
        assertNull(TimeQueryParser.parse("time in atlantis", today))
    }

    @Test
    fun `ordinary text is not a time query`() {
        assertNull(TimeQueryParser.parse("chrome", today))
        assertNull(TimeQueryParser.parse("call mum", today))
        assertNull(TimeQueryParser.parse("", today))
    }

    @Test
    fun `a bare city name is not a time query`() {
        // "paris" should stay available to other providers rather than being claimed here.
        assertNull(TimeQueryParser.parse("paris", today))
    }
}
