package com.abrarshakhi.lumen.feature.notes

import com.abrarshakhi.lumen.core.domain.repository.Note
import kotlin.test.Test
import kotlin.test.assertEquals

class NoteDisplayTitleTest {

    private fun note(title: String, body: String) = Note(1, title, body, 0, 0)

    @Test
    fun `an explicit title wins`() {
        assertEquals("Shopping", note("Shopping", "milk").displayTitle)
    }

    @Test
    fun `without a title the first body line is used`() {
        assertEquals("milk", note("", "milk\nbread").displayTitle)
    }

    @Test
    fun `leading blank lines are skipped`() {
        // Taking the literal first line left the title empty, which is what used to force
        // an "Untitled" placeholder into the provider.
        assertEquals("milk", note("", "\n\n  milk\nbread").displayTitle)
        assertEquals("milk", note("   ", "\nmilk").displayTitle)
    }
}
