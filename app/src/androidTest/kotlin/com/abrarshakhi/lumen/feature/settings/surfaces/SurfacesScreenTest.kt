package com.abrarshakhi.lumen.feature.settings.surfaces

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.theme.LumenTheme
import org.junit.Rule
import org.junit.Test

class SurfacesScreenTest {

    @get:Rule
    val compose = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun widgetPreviewsRenderWithTheAppIcon() {
        compose.setContent {
            LumenTheme {
                SurfacesScreen(
                    state = SurfacesState(canPinWidgets = true, canRequestTile = true),
                    onIntent = {},
                    onBack = {},
                    onOpenLauncherMode = {},
                    snackbarHostState = SnackbarHostState(),
                )
            }
        }

        compose.onNodeWithText(context.getString(R.string.widget_search_label)).assertIsDisplayed()
        compose.onAllNodesWithText(context.getString(R.string.surfaces_add_widget)).onFirst().assertIsDisplayed()
    }
}
