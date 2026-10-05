package com.abrarshakhi.lumen.feature.settings

import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.mvi.MviAction
import com.abrarshakhi.lumen.core.mvi.MviEffect
import com.abrarshakhi.lumen.core.mvi.MviIntent
import com.abrarshakhi.lumen.core.mvi.MviState

sealed interface SettingsIntent : MviIntent {
    data class ThemeModeSelected(val mode: ThemeMode) : SettingsIntent
    data class DynamicColorSet(val enabled: Boolean) : SettingsIntent
    data class FontScaleSet(val scale: Float) : SettingsIntent
    data class SearchBarPositionSelected(val position: SearchBarPosition) : SettingsIntent
}

sealed interface SettingsAction : MviAction {
    data class PreferencesLoaded(val preferences: UserPreferences) : SettingsAction
}

/**
 * Settings state is just the preferences.
 *
 * There is no separate "saving" flag: writes go to DataStore and come back through the same
 * preferences flow, so the switch the user just flipped reflects what was actually persisted
 * rather than an optimistic guess that could diverge from it.
 */
@Immutable
data class SettingsState(
    val preferences: UserPreferences = UserPreferences.Default,
) : MviState

sealed interface SettingsEffect : MviEffect
