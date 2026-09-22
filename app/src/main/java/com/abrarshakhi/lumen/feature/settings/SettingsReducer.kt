package com.abrarshakhi.lumen.feature.settings

import com.abrarshakhi.lumen.core.mvi.Reducer

object SettingsReducer : Reducer<SettingsState, SettingsAction> {
    override fun reduce(state: SettingsState, action: SettingsAction): SettingsState =
        when (action) {
            is SettingsAction.PreferencesLoaded -> state.copy(preferences = action.preferences)
        }
}
