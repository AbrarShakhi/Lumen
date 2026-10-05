package com.abrarshakhi.lumen.feature.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.platform.app.InstrumentationRegistry
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.rank.ResultSection
import com.abrarshakhi.lumen.core.domain.search.PermissionRequest
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.text.TextValue
import com.abrarshakhi.lumen.core.ui.theme.LumenTheme
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests the stateless screen with a fabricated state — no DI, no ViewModel, no engine.
 *
 * The panel dismissal cases exist because both were real bugs: a `clickable` scrim stole
 * focus from the search field (keyboard up, field dead), and the panel did not consume taps,
 * so touching the field closed the surface. Neither shows up in a unit test.
 */
class SearchScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    private fun result(id: String, title: String) = SearchResult(
        id = ResultId(id),
        providerId = ProviderId("apps"),
        title = title,
        icon = IconSource.Vector(LumenIcon.App),
        category = ResultCategory.App,
        score = 1f,
        actions = ResultActions(
            primary = ResultAction(
                id = "open",
                label = TextValue.Raw("Open"),
                icon = IconSource.Vector(LumenIcon.Open),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Handled },
            ),
        ),
    )

    private fun stateWithResults() = SearchState(
        query = "yt",
        sections = listOf(
            ResultSection(
                category = ResultCategory.App,
                results = listOf(result("a", "YouTube"), result("b", "Play Store")),
                order = ResultCategory.App.displayOrder,
            ),
        ),
    )

    private fun setScreen(
        state: SearchState,
        presentation: SearchPresentation,
        onIntent: (SearchIntent) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        compose.setContent {
            LumenTheme {
                SearchScreen(
                    state = state,
                    textFieldState = TextFieldState(state.query),
                    onIntent = onIntent,
                    presentation = presentation,
                    onDismiss = onDismiss,
                    // The IME stealing focus makes touch injection flaky in tests.
                    autoFocus = false,
                )
            }
        }
    }

    // --- Fullscreen ---------------------------------------------------------------------

    @Test
    fun fullscreen_showsIdleMessageWhenQueryIsBlank() {
        setScreen(SearchState(), SearchPresentation.Fullscreen)

        compose.onNodeWithText(context.getString(R.string.search_idle_title)).assertIsDisplayed()
    }

    @Test
    fun fullscreen_showsResultsAndSectionHeader() {
        setScreen(stateWithResults(), SearchPresentation.Fullscreen)

        compose.onNodeWithText(context.getString(R.string.section_app)).assertIsDisplayed()
        compose.onNodeWithText("YouTube").assertIsDisplayed()
        compose.onNodeWithText("Play Store").assertIsDisplayed()
    }

    @Test
    fun fullscreen_tappingResultActivatesIt() {
        val intents = mutableListOf<SearchIntent>()
        setScreen(stateWithResults(), SearchPresentation.Fullscreen, onIntent = { intents += it })

        compose.onNodeWithText("YouTube").performClick()

        assertEquals(
            SearchIntent.ResultActivated(ResultId("a")),
            intents.filterIsInstance<SearchIntent.ResultActivated>().single(),
        )
    }

    // --- Permission prompts ---------------------------------------------------------------

    @Test
    fun permissionPrompt_rendersInlineWhereResultsWouldBe() {
        setScreen(
            SearchState(
                query = "ami",
                permissionRequests = listOf(
                    PermissionRequest(
                        providerId = ProviderId("contacts"),
                        providerName = TextValue.Raw("Contacts"),
                        permissions = listOf(AppPermission.ReadContacts),
                    ),
                ),
            ),
            SearchPresentation.Fullscreen,
        )

        compose.onNodeWithText(context.getString(R.string.permission_title, "Contacts"))
            .assertIsDisplayed()
    }

    @Test
    fun permissionPrompt_tappingItRequestsPermission() {
        val intents = mutableListOf<SearchIntent>()
        setScreen(
            SearchState(
                query = "ami",
                permissionRequests = listOf(
                    PermissionRequest(
                        providerId = ProviderId("contacts"),
                        providerName = TextValue.Raw("Contacts"),
                        permissions = listOf(AppPermission.ReadContacts),
                    ),
                ),
            ),
            SearchPresentation.Fullscreen,
            onIntent = { intents += it },
        )

        compose.onNodeWithText(context.getString(R.string.permission_title, "Contacts")).performClick()

        assertEquals(
            SearchIntent.PermissionRequested(ProviderId("contacts")),
            intents.filterIsInstance<SearchIntent.PermissionRequested>().single(),
        )
    }

    @Test
    fun permissionPrompt_canBeDismissed() {
        val intents = mutableListOf<SearchIntent>()
        setScreen(
            SearchState(
                query = "ami",
                permissionRequests = listOf(
                    PermissionRequest(
                        providerId = ProviderId("contacts"),
                        providerName = TextValue.Raw("Contacts"),
                        permissions = listOf(AppPermission.ReadContacts),
                    ),
                ),
            ),
            SearchPresentation.Fullscreen,
            onIntent = { intents += it },
        )

        compose.onNodeWithContentDescription(context.getString(R.string.permission_dismiss))
            .performClick()

        assertEquals(
            SearchIntent.PermissionPromptDismissed(ProviderId("contacts")),
            intents.filterIsInstance<SearchIntent.PermissionPromptDismissed>().single(),
        )
    }

    // --- Panel --------------------------------------------------------------------------

    @Test
    fun panel_showsResults() {
        setScreen(stateWithResults(), SearchPresentation.Panel)

        compose.onNodeWithText("YouTube").assertIsDisplayed()
    }

    @Test
    fun panel_tappingScrimDismisses() {
        var dismissed = false
        setScreen(stateWithResults(), SearchPresentation.Panel, onDismiss = { dismissed = true })

        // Well above the bottom-anchored card: this is scrim.
        compose.onRoot().performTouchInput { click(Offset(centerX, top + 40f)) }
        compose.waitForIdle()

        assertTrue(dismissed, "tapping away from the panel should dismiss it")
    }

    @Test
    fun panel_tappingInsidePanelDoesNotDismiss() {
        var dismissed = false
        setScreen(stateWithResults(), SearchPresentation.Panel, onDismiss = { dismissed = true })

        // The section header sits inside the card and has no click handler of its own,
        // so this exercises the panel's tap-swallowing rather than a button.
        compose.onNodeWithText(context.getString(R.string.section_app)).performClick()
        compose.waitForIdle()

        assertFalse(dismissed, "taps on the panel must not fall through to the dismiss scrim")
    }

    @Test
    fun panel_tappingResultStillActivatesIt() {
        val intents = mutableListOf<SearchIntent>()
        var dismissed = false
        setScreen(
            stateWithResults(),
            SearchPresentation.Panel,
            onIntent = { intents += it },
            onDismiss = { dismissed = true },
        )

        compose.onNodeWithText("YouTube").performClick()
        compose.waitForIdle()

        assertEquals(1, intents.filterIsInstance<SearchIntent.ResultActivated>().size)
        assertFalse(dismissed, "activating a result is not a dismiss gesture")
    }
}
