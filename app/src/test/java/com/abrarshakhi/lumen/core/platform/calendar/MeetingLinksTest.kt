package com.abrarshakhi.lumen.core.platform.calendar

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * The "join call" action is the main reason to search for a meeting, so link detection has
 * to work against the messy text real invitations contain.
 */
class MeetingLinksTest {

    @Test
    fun `finds google meet links`() {
        assertEquals(
            "https://meet.google.com/abc-defg-hij",
            MeetingLinks.find("https://meet.google.com/abc-defg-hij"),
        )
    }

    @Test
    fun `finds zoom links with a query string`() {
        assertEquals(
            "https://us02web.zoom.us/j/1234567890?pwd=abcDEF",
            MeetingLinks.find("Join: https://us02web.zoom.us/j/1234567890?pwd=abcDEF"),
        )
    }

    @Test
    fun `finds teams and webex and jitsi`() {
        assertEquals(
            "https://teams.microsoft.com/l/meetup-join/19%3ameeting_abc",
            MeetingLinks.find("https://teams.microsoft.com/l/meetup-join/19%3ameeting_abc"),
        )
        assertEquals(
            "https://company.webex.com/meet/someone",
            MeetingLinks.find("https://company.webex.com/meet/someone"),
        )
        assertEquals("https://meet.jit.si/LumenStandup", MeetingLinks.find("https://meet.jit.si/LumenStandup"))
    }

    @Test
    fun `location is searched before description`() {
        // A link in the location field was put there deliberately.
        assertEquals(
            "https://meet.google.com/aaa-bbbb-ccc",
            MeetingLinks.find(
                "https://meet.google.com/aaa-bbbb-ccc",
                "fallback https://meet.google.com/zzz-zzzz-zzz",
            ),
        )
    }

    @Test
    fun `falls back to the description`() {
        assertEquals(
            "https://meet.google.com/zzz-zzzz-zzz",
            MeetingLinks.find("Meeting room 3", "Dial in: https://meet.google.com/zzz-zzzz-zzz"),
        )
    }

    @Test
    fun `finds a link embedded in surrounding prose`() {
        val description = """
            Agenda attached.
            Video call: https://meet.google.com/xyz-abcd-efg
            Or dial +1 555 0100.
        """.trimIndent()

        assertEquals("https://meet.google.com/xyz-abcd-efg", MeetingLinks.find(null, description))
    }

    @Test
    fun `trailing punctuation is not captured as part of the url`() {
        // "See https://meet.google.com/abc-defg-hij." would otherwise yield a broken link.
        assertEquals(
            "https://meet.google.com/abc-defg-hij",
            MeetingLinks.find("See https://meet.google.com/abc-defg-hij."),
        )
    }

    @Test
    fun `ordinary locations yield nothing`() {
        assertNull(MeetingLinks.find("Meeting room 3", "Bring the printouts"))
        assertNull(MeetingLinks.find(null, null))
        assertNull(MeetingLinks.find("", ""))
    }

    @Test
    fun `an unrelated url is not treated as a meeting`() {
        // Offering "Join call" for a docs link would be misleading.
        assertNull(MeetingLinks.find("https://example.com/agenda.pdf"))
    }
}
