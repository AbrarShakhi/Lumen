package com.abrarshakhi.lumen.core.domain.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Formats computed numbers for display.
 *
 * Shared between the calculator and the unit converter because both hit the same problem:
 * binary floating point makes `0.1 + 0.2` print as `0.30000000000000004`, which looks
 * broken even though the arithmetic is correct. Rounding to a fixed significant-figure
 * budget before trimming hides the representation without hiding real precision.
 */
object NumberFormatting {

    /** Beyond this, digits are representation noise rather than information. */
    private const val SIGNIFICANT_DIGITS = 12

    /**
     * Decimal places kept in the displayed answer.
     *
     * Separate from [SIGNIFICANT_DIGITS], which exists only to absorb binary floating-point
     * error. Twelve significant digits is the right budget for that, but showing all of
     * them turns "20cm in inches" into 7.87401574803 — technically exact and practically
     * unreadable. Six is enough for anything a launcher is asked.
     */
    private const val MAX_DECIMALS = 6

    private const val SCIENTIFIC_UPPER = 1e12
    private const val SCIENTIFIC_LOWER = 1e-6

    fun format(value: Double, groupDigits: Boolean = true): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        if (value == 0.0) return "0"

        val magnitude = abs(value)
        if (magnitude >= SCIENTIFIC_UPPER || magnitude < SCIENTIFIC_LOWER) {
            return formatScientific(value)
        }

        val rounded = BigDecimal(value)
            .round(MathContext(SIGNIFICANT_DIGITS, RoundingMode.HALF_UP))
            .setScale(MAX_DECIMALS, RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val plain = rounded.toPlainString()
        return if (groupDigits) groupIntegerPart(plain) else plain
    }

    private fun formatScientific(value: Double): String =
        BigDecimal(value)
            .round(MathContext(6, RoundingMode.HALF_UP))
            .stripTrailingZeros()
            .toString()
            // BigDecimal writes E+15; the shorter form reads better in a result row.
            .replace("E+", "e")
            .replace("E", "e")

    /** Inserts thin separators so long integers stay readable: 1234567 becomes 1,234,567. */
    private fun groupIntegerPart(plain: String): String {
        val negative = plain.startsWith("-")
        val body = if (negative) plain.substring(1) else plain
        val dot = body.indexOf('.')
        val integerPart = if (dot >= 0) body.substring(0, dot) else body
        val fractionPart = if (dot >= 0) body.substring(dot) else ""

        if (integerPart.length <= 4) return plain

        val grouped = integerPart.reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()

        return buildString {
            if (negative) append('-')
            append(grouped)
            append(fractionPart)
        }
    }
}
