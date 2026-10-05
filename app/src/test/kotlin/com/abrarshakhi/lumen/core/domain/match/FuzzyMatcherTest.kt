package com.abrarshakhi.lumen.core.domain.match

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The specification of Lumen's matching behaviour.
 *
 * These are the assertions that define what "the search feels right" means, so they are
 * expressed in terms of user-visible outcomes ("typing gm finds Google Maps") rather than
 * exact scores, which are free to be tuned.
 */
class FuzzyMatcherTest {

    private fun match(query: String, label: String): MatchScore? =
        FuzzyMatcher.score(TextNormalizer.normalize(query), SearchableText.of(label))

    private fun scoreOf(query: String, label: String): Float =
        assertNotNull(match(query, label), "expected '$query' to match '$label'").score

    // --- Structural tiers ---------------------------------------------------------------

    @Test
    fun `exact match scores highest`() {
        assertEquals(1.0f, scoreOf("chrome", "Chrome"))
    }

    /**
     * Tier ordering is only meaningful *within a single query* — that is the only case
     * ranking ever compares. Comparing scores across different queries says nothing.
     */
    @Test
    fun `for one query, stronger structural evidence wins`() {
        // "gm": a literal prefix of "Gmail" is stronger evidence than an acronym of "Google Maps".
        val prefix = scoreOf("gm", "Gmail")
        val acronym = scoreOf("gm", "Google Maps")
        assertTrue(prefix > acronym, "prefix $prefix should beat acronym $acronym")

        // "maps": an exact match beats the same word appearing later in a longer label.
        val exact = scoreOf("maps", "Maps")
        val wordPrefix = scoreOf("maps", "Google Maps")
        assertTrue(exact > wordPrefix, "exact $exact should beat word-prefix $wordPrefix")

        // "doc": a head prefix beats the same characters reached as a scattered subsequence.
        val headPrefix = scoreOf("doc", "Docs")
        val subsequence = scoreOf("doc", "Dungeon of Chaos")
        assertTrue(headPrefix > subsequence, "prefix $headPrefix should beat subsequence $subsequence")
    }

    @Test
    fun `acronym matching finds multi-word apps`() {
        assertNotNull(match("gm", "Google Maps"))
        assertNotNull(match("gps", "Google Play Store"))
        assertNotNull(match("vsc", "Visual Studio Code"))
    }

    @Test
    fun `camel case counts as a word boundary`() {
        // "YouTube" is one token but two words to a human.
        assertNotNull(match("yt", "YouTube"))
    }

    @Test
    fun `earlier substring matches outrank later ones`() {
        val early = scoreOf("book", "Booking")
        val late = scoreOf("book", "Facebook")
        assertTrue(early > late, "'Booking' ($early) should outrank 'Facebook' ($late) for 'book'")
    }

    @Test
    fun `subsequence matches non-adjacent characters`() {
        assertNotNull(match("gdocs", "Google Docs"))
        assertNotNull(match("stngs", "Settings"))
    }

    // --- Typo tolerance -----------------------------------------------------------------

    @Test
    fun `transposed characters still match`() {
        assertNotNull(match("chrmoe", "Chrome"))
    }

    @Test
    fun `single substitution still matches`() {
        assertNotNull(match("telegran", "Telegram"))
    }

    @Test
    fun `typo tolerance ranks below every structural match`() {
        val typo = scoreOf("chrmoe", "Chrome")
        val subsequence = scoreOf("gdocs", "Google Docs")
        assertTrue(typo < subsequence, "typo $typo must rank below subsequence $subsequence")
    }

    @Test
    fun `short queries do not get typo tolerance`() {
        // Allowing edits on 3 characters would match almost anything to almost anything.
        assertNull(match("xyz", "Chrome"))
    }

    // --- Normalisation ------------------------------------------------------------------

    @Test
    fun `diacritics are ignored in both directions`() {
        assertNotNull(match("cafe", "Café"))
        assertNotNull(match("café", "Cafe"))
    }

    @Test
    fun `matching is case insensitive`() {
        assertEquals(scoreOf("CHROME", "Chrome"), scoreOf("chrome", "chrome"))
    }

    // --- Negative cases -----------------------------------------------------------------

    @Test
    fun `unrelated queries do not match`() {
        assertNull(match("spotify", "Calculator"))
        assertNull(match("zzzz", "Google Maps"))
    }

    @Test
    fun `out of order characters do not match`() {
        // Subsequence requires order: "emorhc" is "chrome" backwards.
        assertNull(match("emorhc", "Chrome"))
    }

    @Test
    fun `empty candidate never matches`() {
        assertNull(match("anything", ""))
    }

    // --- Highlight ranges ---------------------------------------------------------------

    @Test
    fun `prefix match highlights the matched span`() {
        val result = assertNotNull(match("chr", "Chrome"))
        assertEquals(listOf(0 until 3), result.ranges)
    }

    @Test
    fun `word prefix highlights the matched word, not the start`() {
        val result = assertNotNull(match("maps", "Google Maps"))
        assertEquals(listOf(7 until 11), result.ranges)
    }

    @Test
    fun `contiguous subsequence indices collapse into one range`() {
        val result = assertNotNull(match("goog", "Google Maps"))
        assertEquals(1, result.ranges.size, "adjacent matches should collapse: ${result.ranges}")
    }

    @Test
    fun `highlight ranges stay inside the candidate`() {
        for (query in listOf("gm", "maps", "gdocs", "chrmoe")) {
            val label = if (query == "chrmoe") "Chrome" else "Google Maps"
            val result = match(query, label) ?: continue
            val normalized = SearchableText.of(label).normalized
            result.ranges.forEach { range ->
                assertTrue(
                    range.first >= 0 && range.last < normalized.length,
                    "range $range out of bounds for '$normalized' (query '$query')",
                )
            }
        }
    }
}
