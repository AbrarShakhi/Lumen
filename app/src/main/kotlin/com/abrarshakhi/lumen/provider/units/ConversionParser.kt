package com.abrarshakhi.lumen.provider.units

object ConversionParser {

    data class Conversion(
        val value: Double,
        val from: MeasurementUnit,
        val to: MeasurementUnit,
    ) {
        val result: Double get() = to.fromBaseValue(from.toBaseValue(value))
    }

    private val PATTERN = Regex(
        """^\s*([-+]?\d[\d,]*\.?\d*)\s*([a-zA-Z°/²³"]+)\s+(?:in|to|as|into)\s+([a-zA-Z°/²³"]+)\s*$""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(input: String): Conversion? {
        val match = PATTERN.find(input) ?: return null

        val (rawValue, rawFrom, rawTo) = match.destructured

        val value = rawValue.replace(",", "").toDoubleOrNull() ?: return null
        val from = UnitRegistry.find(rawFrom) ?: return null
        val to = UnitRegistry.find(rawTo) ?: return null

        if (from.dimension != to.dimension) return null

        return Conversion(value, from, to)
    }
}
