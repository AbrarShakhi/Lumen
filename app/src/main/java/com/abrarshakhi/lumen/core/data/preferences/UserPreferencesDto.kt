package com.abrarshakhi.lumen.core.data.preferences

import com.abrarshakhi.lumen.core.domain.preferences.SearchBarPosition
import com.abrarshakhi.lumen.core.domain.preferences.ThemeMode
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import kotlinx.serialization.Serializable

/**
 * The on-disk form of [UserPreferences].
 *
 * Separate from the domain model so that persistence concerns — tolerating unknown keys,
 * defaulting absent ones, renaming a field without touching business code — stay in the
 * data layer. Enums are stored as strings and decoded defensively, because an enum
 * constant removed in a future version must not make the whole file unreadable.
 */
@Serializable
internal data class UserPreferencesDto(
    val themeMode: String = ThemeMode.System.name,
    val dynamicColor: Boolean = true,
    val fontScale: Float = 1.0f,
    val searchBarPosition: String = SearchBarPosition.Bottom.name,
    val enabledProviders: Map<String, Boolean> = emptyMap(),
    val dismissedPermissionPrompts: Set<String> = emptySet(),
    val onboardingCompleted: Boolean = false,
) {
    fun toDomain(): UserPreferences = UserPreferences(
        themeMode = enumOrDefault(themeMode, ThemeMode.System),
        dynamicColor = dynamicColor,
        fontScale = fontScale.coerceIn(UserPreferences.MIN_FONT_SCALE, UserPreferences.MAX_FONT_SCALE),
        searchBarPosition = enumOrDefault(searchBarPosition, SearchBarPosition.Bottom),
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
            enabledProviders = preferences.enabledProviders,
            dismissedPermissionPrompts = preferences.dismissedPermissionPrompts,
            onboardingCompleted = preferences.onboardingCompleted,
        )

        private inline fun <reified E : Enum<E>> enumOrDefault(name: String, fallback: E): E =
            enumValues<E>().firstOrNull { it.name == name } ?: fallback
    }
}
