package com.abrarshakhi.lumen.core.data.preferences

import com.abrarshakhi.lumen.core.domain.preferences.ColorStyle
import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeAccent
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import kotlinx.serialization.Serializable

@Serializable
internal data class UserPreferencesDto(
    val themeMode: String = ThemeMode.System.name,
    val dynamicColor: Boolean = true,
    val fontScale: Float = 1.0f,
    val searchBarPosition: String = SearchBarPosition.Bottom.name,
    val accent: String = ThemeAccent.Ember.name,
    val colorStyle: String = ColorStyle.TonalSpot.name,
    val pureBlack: Boolean = false,
    val enabledProviders: Map<String, Boolean> = emptyMap(),
    val dismissedPermissionPrompts: Set<String> = emptySet(),
    val onboardingCompleted: Boolean = false,
) {
    fun toDomain(): UserPreferences = UserPreferences(
        themeMode = enumOrDefault(themeMode, ThemeMode.System),
        dynamicColor = dynamicColor,
        fontScale = fontScale.coerceIn(UserPreferences.MIN_FONT_SCALE, UserPreferences.MAX_FONT_SCALE),
        searchBarPosition = enumOrDefault(searchBarPosition, SearchBarPosition.Bottom),
        accent = enumOrDefault(accent, ThemeAccent.Ember),
        colorStyle = enumOrDefault(colorStyle, ColorStyle.TonalSpot),
        pureBlack = pureBlack,
        enabledProviders = enabledProviders,
        dismissedPermissionPrompts = dismissedPermissionPrompts,
        onboardingCompleted = onboardingCompleted,
    )

    companion object {
        fun fromDomain(preferences: UserPreferences) = UserPreferencesDto(
            themeMode = preferences.themeMode.name,
            dynamicColor = preferences.dynamicColor,
            fontScale = preferences.fontScale,
            searchBarPosition = preferences.searchBarPosition.name,
            accent = preferences.accent.name,
            colorStyle = preferences.colorStyle.name,
            pureBlack = preferences.pureBlack,
            enabledProviders = preferences.enabledProviders,
            dismissedPermissionPrompts = preferences.dismissedPermissionPrompts,
            onboardingCompleted = preferences.onboardingCompleted,
        )

        private inline fun <reified E : Enum<E>> enumOrDefault(name: String, fallback: E): E =
            enumValues<E>().firstOrNull { it.name == name } ?: fallback
    }
}
