package com.abrarshakhi.lumen.feature.settings

import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsReducerTest {

    @Test
    fun `loaded preferences replace state`() {
        val loaded = UserPreferences(
            themeMode = ThemeMode.Dark,
            dynamicColor = false,
            fontScale = 1.15f,
            searchBarPosition = SearchBarPosition.Top,
        )

        val state = SettingsReducer.reduce(
            SettingsState(),
            SettingsAction.PreferencesLoaded(loaded),
        )

        assertEquals(loaded, state.preferences)
    }
}
