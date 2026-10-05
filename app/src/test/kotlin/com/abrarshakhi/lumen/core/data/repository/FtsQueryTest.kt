package com.abrarshakhi.lumen.core.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FtsQueryTest {

    @Test
    fun `terms get a prefix wildcard so partial words match`() {
        assertEquals("shopping*", FtsQuery.sanitize("shopping"))
        assertEquals("shop* list*", FtsQuery.sanitize("shop list"))
    }

    @Test
    fun `fts operators are stripped rather than passed through`() {
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
        assertNull(FtsQuery.sanitize(""))
        assertNull(FtsQuery.sanitize("   "))
        assertNull(FtsQuery.sanitize("\"\"\""))
        assertNull(FtsQuery.sanitize("---"))
    }
}
