package com.abrarshakhi.lumen.provider.units

/**
 * Recognises "20cm in inches" and its variants.
 *
 * Pure and Android-free so the matching rules — which decide when typing turns into a
 * conversion rather than staying a plain search — are exhaustively testable.
 */
object ConversionParser {

    data class Conversion(
        val value: Double,
        val from: MeasurementUnit,
        val to: MeasurementUnit,
    ) {
        val result: Double get() = to.fromBaseValue(from.toBaseValue(value))
    }

    /**
     * `<number> <unit> (in|to|as) <unit>`, with the space between number and unit optional.
     *
     * Unit tokens allow the punctuation real units contain (`°`, `/`, `²`, `"`), so `km/h`
     * and `°F` parse as single tokens rather than being split.
     */
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

        // "5 kg in miles" is a typo, not a request — converting across dimensions would
        // produce a confidently wrong number.
        if (from.dimension != to.dimension) return null

        return Conversion(value, from, to)
    }
}
