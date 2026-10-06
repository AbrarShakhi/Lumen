package com.abrarshakhi.lumen.feature.settings

import com.abrarshakhi.lumen.core.domain.preferences.ColorStyle
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeAccent
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

    @Test
    fun `colour choices are persisted`() = runTest {
        val preferences = FakePreferences()
        val viewModel = SettingsViewModel(preferences)
        advanceUntilIdle()

        viewModel.dispatch(SettingsIntent.AccentSelected(ThemeAccent.Ocean))
        viewModel.dispatch(SettingsIntent.ColorStyleSelected(ColorStyle.Expressive))
        viewModel.dispatch(SettingsIntent.PureBlackSet(true))
        advanceUntilIdle()

        val stored = preferences.preferences.value
        assertEquals(ThemeAccent.Ocean, stored.accent)
        assertEquals(ColorStyle.Expressive, stored.colorStyle)
        assertEquals(true, stored.pureBlack)
        assertEquals(stored, viewModel.state.value.preferences)
    }
}
