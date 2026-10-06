package com.abrarshakhi.lumen.feature.settings.surfaces

import app.cash.turbine.test
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.platform.HomeScreenSurfaces
import com.abrarshakhi.lumen.core.domain.platform.HomeWidget
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.platform.TileRequestResult
import com.abrarshakhi.lumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SurfacesViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakeSurfaces(
        override val canPinWidgets: Boolean = true,
        override val canRequestTile: Boolean = true,
        private val pinAccepted: Boolean = true,
        private val tileResult: TileRequestResult = TileRequestResult.Added,
    ) : HomeScreenSurfaces {
        val pinned = mutableListOf<HomeWidget>()

        override fun pinWidget(widget: HomeWidget): Boolean {
            pinned += widget
            return pinAccepted
        }

        override suspend fun requestTile(): TileRequestResult = tileResult
    }

    private class FakeLauncher(private val succeeds: Boolean = true) : IntentLauncher {
        val launched = mutableListOf<PlatformIntent>()

        override suspend fun launch(intent: PlatformIntent): Boolean {
            launched += intent
            return succeeds
        }
    }

    @Test
    fun `capabilities are reported in state`() = runTest {
        val viewModel = SurfacesViewModel(
            FakeSurfaces(canPinWidgets = false, canRequestTile = true),
            FakeLauncher(),
        )
        advanceUntilIdle()

        assertEquals(SurfacesState(canPinWidgets = false, canRequestTile = true), viewModel.state.value)
    }

    @Test
    fun `pinning asks the platform and stays quiet when it accepts`() = runTest {
        val surfaces = FakeSurfaces()
        val viewModel = SurfacesViewModel(surfaces, FakeLauncher())
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.dispatch(SurfacesIntent.PinWidget(HomeWidget.Notes))
            advanceUntilIdle()
            expectNoEvents()
        }
        assertEquals(listOf(HomeWidget.Notes), surfaces.pinned)
    }

    @Test
    fun `a launcher that refuses pinning explains the manual route`() = runTest {
        val viewModel = SurfacesViewModel(FakeSurfaces(pinAccepted = false), FakeLauncher())
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.dispatch(SurfacesIntent.PinWidget(HomeWidget.Search))
            assertEquals(SurfacesEffect.ShowMessage(R.string.surfaces_pin_unsupported), awaitItem())
        }
    }

    @Test
    fun `tile request outcomes map to messages`() = runTest {
        val viewModel = SurfacesViewModel(
            FakeSurfaces(tileResult = TileRequestResult.AlreadyAdded),
            FakeLauncher(),
        )
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.dispatch(SurfacesIntent.AddTile)
            assertEquals(SurfacesEffect.ShowMessage(R.string.surfaces_tile_already_added), awaitItem())
        }
    }

    @Test
    fun `assistant settings fall back to a message when unavailable`() = runTest {
        val launcher = FakeLauncher(succeeds = false)
        val viewModel = SurfacesViewModel(FakeSurfaces(), launcher)
        advanceUntilIdle()

        viewModel.effects.test {
            viewModel.dispatch(SurfacesIntent.OpenAssistantSettings)
            assertEquals(
                SurfacesEffect.ShowMessage(R.string.surfaces_assistant_unavailable),
                awaitItem(),
            )
        }
        assertEquals(
            listOf<PlatformIntent>(PlatformIntent.SystemSetting("android.settings.VOICE_INPUT_SETTINGS")),
            launcher.launched,
        )
    }
}
