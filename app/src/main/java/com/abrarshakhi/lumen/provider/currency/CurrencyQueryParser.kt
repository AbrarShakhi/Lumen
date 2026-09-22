package com.abrarshakhi.lumen.provider.currency

/**
 * Recognises "100 usd in eur" and friends.
 *
 * Kept separate from the unit converter because currencies are not units: their rates
 * change, so conversion needs a network round trip and a cache, while a metre is always a
 * metre. Sharing the parser would have coupled an offline feature to a networked one.
 */
object CurrencyQueryParser {

    data class Request(val amount: Double, val from: String, val to: String)

    /**
     * Currency codes are recognised only as exactly three letters, upper or lower case.
     *
     * A deliberately tight rule: loosening it would make "20 cm in in" look like a currency
     * request, and a query that two providers both claim produces duplicate answers.
     */
    private val PATTERN = Regex(
        """^\s*([-+]?\d[\d,]*\.?\d*)\s*([a-zA-Z]{3})\s+(?:in|to|as|into)\s+([a-zA-Z]{3})\s*$""",
        RegexOption.IGNORE_CASE,
    )

    fun parse(input: String, known: Set<String>): Request? {
        val match = PATTERN.find(input) ?: return null
        val (rawAmount, rawFrom, rawTo) = match.destructured

        val from = rawFrom.uppercase()
        val to = rawTo.uppercase()
        // Both sides must be currencies Lumen actually has rates for, otherwise this is
        // some other kind of query that happens to fit the shape.
        if (from !in known || to !in known) return null
        if (from == to) return null

        val amount = rawAmount.replace(",", "").toDoubleOrNull() ?: return null
        return Request(amount, from, to)
    }
}
