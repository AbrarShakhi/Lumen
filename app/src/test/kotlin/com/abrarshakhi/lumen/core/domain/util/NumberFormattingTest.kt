package com.abrarshakhi.lumen.core.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals

class NumberFormattingTest {

    @Test
    fun `floating point noise is hidden`() {
        // The whole reason this class exists: 0.1 + 0.2 must not print as 0.30000000000000004.
        assertEquals("0.3", NumberFormatting.format(0.1 + 0.2))
        assertEquals("3", NumberFormatting.format(2.9999999999999996))
    }

    @Test
    fun `integers print without a decimal point`() {
        assertEquals("4", NumberFormatting.format(4.0))
        assertEquals("-7", NumberFormatting.format(-7.0))
        assertEquals("0", NumberFormatting.format(0.0))
    }

    @Test
    fun `long integers are grouped`() {
        assertEquals("1,234,567", NumberFormatting.format(1234567.0))
        assertEquals("-1,234,567", NumberFormatting.format(-1234567.0))
    }

    @Test
    fun `four digit numbers are left ungrouped`() {
        // Years and small counts read better without a separator.
        assertEquals("2026", NumberFormatting.format(2026.0))
    }

    @Test
    fun `answers are capped at six decimal places`() {
        // 20cm in inches is exactly 7.87401574803…; showing all of it is unreadable.
        assertEquals("7.874016", NumberFormatting.format(7.874015748031496))
        assertEquals("0.333333", NumberFormatting.format(1.0 / 3.0))
    }

    @Test
    fun `grouping can be disabled for copying`() {
        assertEquals("1234567", NumberFormatting.format(1234567.0, groupDigits = false))
    }

    @Test
    fun `very large and very small numbers use scientific notation`() {
        assertEquals(true, NumberFormatting.format(1e20).contains("e"))
        assertEquals(true, NumberFormatting.format(1e-9).contains("e"))
    }

    @Test
    fun `non finite values are readable`() {
        assertEquals("∞", NumberFormatting.format(Double.POSITIVE_INFINITY))
        assertEquals("-∞", NumberFormatting.format(Double.NEGATIVE_INFINITY))
        assertEquals("NaN", NumberFormatting.format(Double.NaN))
    }
}
