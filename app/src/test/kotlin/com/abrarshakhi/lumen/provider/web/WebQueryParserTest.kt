package com.abrarshakhi.lumen.provider.web

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * These rules decide when typing turns into a web search, a specific engine, or opening an
 * address — the places where a launcher most easily surprises someone.
 */
class WebQueryParserTest {

    private fun parse(raw: String) = WebQueryParser.parse(raw)

    @Test
    fun `an ordinary query goes to the default engine`() {
        val parsed = assertIs<WebQueryParser.Parsed.Search>(parse("kotlin flows"))
        assertEquals("kotlin flows", parsed.terms)
    }

    @Test
    fun `a keyword routes to its engine and strips itself from the query`() {
        val parsed = assertIs<WebQueryParser.Parsed.Engine>(parse("ggl kotlin flows"))
        assertEquals("google", parsed.engine.id)
        assertEquals("kotlin flows", parsed.terms)
    }

    @Test
    fun `keywords are case insensitive`() {
        val parsed = assertIs<WebQueryParser.Parsed.Engine>(parse("DDG privacy"))
        assertEquals("duckduckgo", parsed.engine.id)
    }

    @Test
    fun `a keyword only counts as a whole first word`() {
        // "ghost" begins with the GitHub keyword "gh" but is plainly not that instruction.
        val parsed = assertIs<WebQueryParser.Parsed.Search>(parse("ghost writer"))
        assertEquals("ghost writer", parsed.terms)
    }

    @Test
    fun `a lone keyword is not yet a search`() {
        // Someone mid-typing "ggl…" should not be offered a search for nothing.
        assertIs<WebQueryParser.Parsed.None>(parse("ggl"))
        assertIs<WebQueryParser.Parsed.None>(parse("ggl   "))
    }

    @Test
    fun `yt is not a web keyword so the YouTube app keeps its acronym`() {
        val parsed = assertIs<WebQueryParser.Parsed.Search>(parse("yt music"))
        assertEquals("yt music", parsed.terms, "'yt' must stay available to the app matcher")
    }

    @Test
    fun `full urls are opened rather than searched`() {
        val parsed = assertIs<WebQueryParser.Parsed.Url>(parse("https://example.com/path"))
        assertEquals("https://example.com/path", parsed.url)
    }

    @Test
    fun `bare domains are opened with a scheme added`() {
        assertEquals("https://github.com", assertIs<WebQueryParser.Parsed.Url>(parse("github.com")).url)
        assertEquals(
            "https://github.com/foo/bar",
            assertIs<WebQueryParser.Parsed.Url>(parse("github.com/foo/bar")).url,
        )
    }

    @Test
    fun `a sentence containing a full stop is not treated as a url`() {
        assertIs<WebQueryParser.Parsed.Search>(parse("hello. how are you"))
        assertIs<WebQueryParser.Parsed.Search>(parse("version 1.2 release notes"))
    }

    @Test
    fun `a single word with no tld is a search, not a url`() {
        assertIs<WebQueryParser.Parsed.Search>(parse("localhost"))
        assertIs<WebQueryParser.Parsed.Search>(parse("settings"))
    }

    @Test
    fun `blank input yields nothing`() {
        assertIs<WebQueryParser.Parsed.None>(parse(""))
        assertIs<WebQueryParser.Parsed.None>(parse("   "))
    }

    @Test
    fun `every built-in engine has unique keywords`() {
        // Overlapping keywords would make routing depend on declaration order.
        val all = WebSearchEngine.BuiltIn.flatMap { it.keywords }
        assertEquals(all.size, all.toSet().size, "duplicate keyword across engines: $all")
    }
}
