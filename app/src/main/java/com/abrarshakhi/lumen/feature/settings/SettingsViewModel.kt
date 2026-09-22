package com.abrarshakhi.lumen.feature.settings

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.mvi.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SettingsViewModel(
    private val preferences: UserPreferencesRepository,
) : MviViewModel<SettingsIntent, SettingsAction, SettingsState, SettingsEffect>(
    initialState = SettingsState(),
    reducer = SettingsReducer,
) {

    init {
        preferences.preferences
            .onEach { reduce(SettingsAction.PreferencesLoaded(it)) }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: SettingsIntent) {
        when (intent) {
            is SettingsIntent.ThemeModeSelected ->
                update { it.copy(themeMode = intent.mode) }

            is SettingsIntent.DynamicColorSet ->
                update { it.copy(dynamicColor = intent.enabled) }

            is SettingsIntent.FontScaleSet -> update {
                it.copy(
                    fontScale = intent.scale.coerceIn(
                        UserPreferences.MIN_FONT_SCALE,
                        UserPreferences.MAX_FONT_SCALE,
                    ),
                )
            }

            is SettingsIntent.SearchBarPositionSelected ->
                update { it.copy(searchBarPosition = intent.position) }
        }
    }

    private suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        preferences.update(transform)
    }
}
