package com.abrarshakhi.lumen.provider.units

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ConversionParserTest {

    private fun convert(input: String): Double =
        assertNotNull(ConversionParser.parse(input), "expected '$input' to parse").result

    @Test
    fun `length conversions`() {
        assertEquals(7.874, convert("20cm in inches"), 0.001)
        assertEquals(1000.0, convert("1 km to m"), 1e-9)
        assertEquals(1.60934, convert("1 mile in km"), 0.0001)
    }

    @Test
    fun `mass conversions`() {
        assertEquals(2.20462, convert("1 kg in lbs"), 0.0001)
        assertEquals(1000.0, convert("1kg to g"), 1e-9)
    }

    @Test
    fun `temperature uses an affine conversion, not a ratio`() {
        // The classic mistake: scaling 100F by 5/9 gives 55.6 instead of 37.8.
        assertEquals(37.7778, convert("100f in c"), 0.001)
        assertEquals(32.0, convert("0 c in f"), 1e-9)
        assertEquals(212.0, convert("100 celsius to fahrenheit"), 1e-9)
        assertEquals(273.15, convert("0 c in k"), 1e-9)
        assertEquals(0.0, convert("273.15 k to c"), 1e-9)
    }

    @Test
    fun `negative temperatures convert correctly`() {
        assertEquals(-40.0, convert("-40 f in c"), 1e-9)
    }

    @Test
    fun `decimal and binary data units are distinct`() {
        assertEquals(1000.0, convert("1 gb in mb"), 1e-9)
        assertEquals(1024.0, convert("1 gib in mib"), 1e-9)
    }

    @Test
    fun `speed conversions`() {
        assertEquals(100.0, convert("27.7778 m/s in km/h"), 0.01)
        assertEquals(1.60934, convert("1 mph in km/h"), 0.001)
    }

    @Test
    fun `all the connecting words work`() {
        for (word in listOf("in", "to", "as", "into")) {
            assertNotNull(ConversionParser.parse("1 km $word m"), "'$word' should connect")
        }
    }

    @Test
    fun `the space between number and unit is optional`() {
        assertEquals(convert("20 cm in in"), convert("20cm in in"), 1e-9)
    }

    @Test
    fun `thousands separators are accepted`() {
        assertEquals(1000.0, convert("1,000 m in m"), 1e-9)
    }

    @Test
    fun `mismatched dimensions are refused`() {
        // Better to decline than to invent a confidently wrong number.
        assertNull(ConversionParser.parse("5 kg in miles"))
        assertNull(ConversionParser.parse("20 c in gb"))
    }

    @Test
    fun `unknown units are refused`() {
        assertNull(ConversionParser.parse("5 smoots in m"))
        assertNull(ConversionParser.parse("5 m in smoots"))
    }

    @Test
    fun `ordinary text is not a conversion`() {
        assertNull(ConversionParser.parse("call mum"))
        assertNull(ConversionParser.parse("chrome"))
        assertNull(ConversionParser.parse(""))
        assertNull(ConversionParser.parse("20cm"))
    }

    @Test
    fun `inch abbreviation survives colliding with the connecting word`() {
        // "20 in in cm" — the first "in" is a unit, the second is the connector.
        assertEquals(50.8, convert("20 in in cm"), 0.001)
    }

    @Test
    fun `every alias is unique across the whole registry`() {
        // A duplicate alias would make conversion depend on declaration order.
        val all = UnitRegistry.units.flatMap { it.aliases }
        val duplicates = all.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        assertEquals(emptySet(), duplicates, "ambiguous unit aliases: $duplicates")
    }

    @Test
    fun `round trips return the original value`() {
        for (unit in UnitRegistry.units) {
            val base = unit.toBaseValue(42.0)
            assertEquals(42.0, unit.fromBaseValue(base), 1e-9, "round trip failed for ${unit.id}")
        }
    }
}
