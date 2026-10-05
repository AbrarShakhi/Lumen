package com.abrarshakhi.lumen.provider.currency

object CurrencyQueryParser {

    data class Request(val amount: Double, val from: String, val to: String)

    private val PATTERN = Regex(
        """^\s*([-+]?\d[\d,]*\.?\d*)\s*([a-zA-Z]{3})\s+(?:in|to|as|into)\s+([a-zA-Z]{3})\s*$""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(input: String, known: Set<String>): Request? {
        val match = PATTERN.find(input) ?: return null
        val (rawAmount, rawFrom, rawTo) = match.destructured

        val from = rawFrom.uppercase()
        val to = rawTo.uppercase()
        if (from !in known || to !in known) return null
        if (from == to) return null

        val amount = rawAmount.replace(",", "").toDoubleOrNull() ?: return null
        return Request(amount, from, to)
    }
}
