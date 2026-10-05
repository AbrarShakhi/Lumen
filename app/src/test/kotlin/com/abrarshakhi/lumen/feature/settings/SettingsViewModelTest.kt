package com.abrarshakhi.lumen.feature.settings

import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private class FakePreferences : UserPreferencesRepository {
        private val state = MutableStateFlow(UserPreferences.Default)
        override val preferences: StateFlow<UserPreferences> = state
        override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
            state.value = transform(state.value)
        }
    }

    @Test
    fun `changing a setting persists it and echoes back through state`() = runTest {
        val preferences = FakePreferences()
        val viewModel = SettingsViewModel(preferences)
        advanceUntilIdle()

        viewModel.dispatch(SettingsIntent.ThemeModeSelected(ThemeMode.Dark))
        viewModel.dispatch(SettingsIntent.SearchBarPositionSelected(SearchBarPosition.Top))
        viewModel.dispatch(SettingsIntent.DynamicColorSet(false))
        advanceUntilIdle()

        assertEquals(ThemeMode.Dark, preferences.preferences.value.themeMode)
        assertEquals(SearchBarPosition.Top, viewModel.state.value.preferences.searchBarPosition)
        assertEquals(false, viewModel.state.value.preferences.dynamicColor)
    }

    @Test
    fun `font scale is clamped to the supported range`() = runTest {
        // The slider cannot produce these, but a restored preference or a future caller
        // could, and an unclamped scale would make the UI unusable.
        val preferences = FakePreferences()
        val viewModel = SettingsViewModel(preferences)
        advanceUntilIdle()

        viewModel.dispatch(SettingsIntent.FontScaleSet(9f))
        advanceUntilIdle()
        assertEquals(UserPreferences.MAX_FONT_SCALE, preferences.preferences.value.fontScale)

        viewModel.dispatch(SettingsIntent.FontScaleSet(0.1f))
        advanceUntilIdle()
        assertEquals(UserPreferences.MIN_FONT_SCALE, preferences.preferences.value.fontScale)
    }
}
