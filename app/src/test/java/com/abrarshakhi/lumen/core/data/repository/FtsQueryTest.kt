package com.abrarshakhi.lumen.core.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * FTS4 has its own query language, so raw typing cannot be passed through: an unbalanced
 * quote or a stray `-` is a syntax error, not a search that finds nothing.
 */
class FtsQueryTest {

    @Test
    fun `terms get a prefix wildcard so partial words match`() {
        assertEquals("shopping*", FtsQuery.sanitize("shopping"))
        assertEquals("shop* list*", FtsQuery.sanitize("shop list"))
    }

    @Test
    fun `fts operators are stripped rather than passed through`() {
        // Each of these would otherwise be interpreted as syntax.
        assertEquals("foo*", FtsQuery.sanitize("\"foo"))
        assertEquals("foo*", FtsQuery.sanitize("foo*"))
        assertEquals("foo* bar*", FtsQuery.sanitize("foo -bar"))
        assertEquals("foo*", FtsQuery.sanitize("foo:"))
        assertEquals("foo*", FtsQuery.sanitize("(foo)"))
        assertEquals("foo*", FtsQuery.sanitize("^foo"))
    }

    @Test
    fun `whitespace is collapsed`() {
        assertEquals("a* b*", FtsQuery.sanitize("  a    b  "))
        assertEquals("a* b*", FtsQuery.sanitize("a\tb"))
    }

    @Test
    fun `nothing searchable yields null rather than an empty match`() {
        // An empty MATCH expression is a SQLite error, so this must not reach the query.
        assertNull(FtsQuery.sanitize(""))
        assertNull(FtsQuery.sanitize("   "))
        assertNull(FtsQuery.sanitize("\"\"\""))
        assertNull(FtsQuery.sanitize("---"))
    }
}
