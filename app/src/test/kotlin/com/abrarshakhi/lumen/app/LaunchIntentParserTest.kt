package com.abrarshakhi.lumen.app

import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Navigation 3 does not parse deep links, so every entry point into Lumen — launcher icon,
 * assistant gesture, tile, widget, another app's search intent — resolves through this one
 * pure function. Keeping it pure is what makes all of it testable without an Activity.
 */
class LaunchIntentParserTest {

    @Test
    fun `a plain launch opens search with the field focused`() {
        val request = parseLaunchIntent(action = LaunchActions.MAIN)

        assertEquals(AppRouteKey.Search, request.route)
        assertNull(request.prefilledQuery)
        assertTrue(request.focusInput)
    }

    @Test
    fun `the assistant gesture opens search`() {
        assertEquals(AppRouteKey.Search, parseLaunchIntent(action = LaunchActions.ASSIST).route)
    }

    @Test
    fun `a handed-over query is prefilled and does not steal focus`() {
        val request = parseLaunchIntent(action = LaunchActions.WEB_SEARCH, query = "kotlin flows")

        assertEquals("kotlin flows", request.prefilledQuery)
        assertFalse(request.focusInput, "the query is already complete; opening the IME would obscure results")
    }

    @Test
    fun `blank and whitespace queries are treated as absent`() {
        assertNull(parseLaunchIntent(action = LaunchActions.SEARCH, query = "   ").prefilledQuery)
        assertNull(parseLaunchIntent(action = LaunchActions.SEARCH, query = "").prefilledQuery)
    }

    @Test
    fun `queries are trimmed`() {
        assertEquals("maps", parseLaunchIntent(action = LaunchActions.SEARCH, query = "  maps  ").prefilledQuery)
    }

    @Test
    fun `incomplete onboarding redirects every entry point`() {
        for (action in listOf(LaunchActions.MAIN, LaunchActions.ASSIST, LaunchActions.WEB_SEARCH)) {
            val request = parseLaunchIntent(action = action, query = "x", onboardingCompleted = false)
            assertEquals(AppRouteKey.Onboarding, request.route, "action $action should be gated")
        }
    }

    @Test
    fun `an unknown action still lands on search`() {
        assertEquals(AppRouteKey.Search, parseLaunchIntent(action = "com.example.SOMETHING").route)
        assertEquals(AppRouteKey.Search, parseLaunchIntent(action = null).route)
    }

    private fun assertFalse(value: Boolean, message: String) = assertTrue(!value, message)
}
