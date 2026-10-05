package com.abrarshakhi.lumen.core.domain.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

object NumberFormatting {

    private const val SIGNIFICANT_DIGITS = 12

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
            .replace("E+", "e")
            .replace("E", "e")

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
