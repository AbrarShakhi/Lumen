package com.abrarshakhi.lumen.provider.currency

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CurrencyQueryParserTest {

    private val known = ExchangeRatesClient.SUPPORTED

    private fun parse(input: String) = CurrencyQueryParser.parse(input, known)

    @Test
    fun `a currency pair is recognised`() {
        val request = assertNotNull(parse("100 usd in eur"))
        assertEquals(100.0, request.amount)
        assertEquals("USD", request.from)
        assertEquals("EUR", request.to)
    }

    @Test
    fun `codes are case insensitive and connectors interchangeable`() {
        for (word in listOf("in", "to", "as", "into")) {
            assertNotNull(parse("5 GBP $word JPY"), "'$word' should connect")
        }
        assertNotNull(parse("5 gbp to jpy"))
    }

    @Test
    fun `decimals and thousands separators are handled`() {
        assertEquals(1234.56, assertNotNull(parse("1,234.56 usd in eur")).amount)
    }

    @Test
    fun `unknown currency codes are refused`() {
        assertNull(parse("100 xyz in eur"))
        assertNull(parse("100 usd in xyz"))
    }

    @Test
    fun `converting a currency to itself is not a question`() {
        assertNull(parse("100 usd in usd"))
    }

    @Test
    fun `unit conversions are not claimed as currency`() {
        // Both providers match "<number> <word> in <word>"; the three-letter rule plus the
        // known-code check is what stops them both answering the same query.
        assertNull(parse("20 cm in in"))
        assertNull(parse("5 kg in lbs"))
        assertNull(parse("100 f in c"))
    }

    @Test
    fun `ordinary text is not a currency query`() {
        assertNull(parse("call mum"))
        assertNull(parse(""))
        assertNull(parse("100 usd"))
    }
}
