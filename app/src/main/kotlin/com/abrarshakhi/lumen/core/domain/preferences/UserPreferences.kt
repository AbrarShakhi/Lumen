package com.abrarshakhi.lumen.core.domain.preferences

import kotlinx.coroutines.flow.StateFlow

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val fontScale: Float = 1.0f,
    val searchBarPosition: SearchBarPosition = SearchBarPosition.Bottom,
    val enabledProviders: Map<String, Boolean> = emptyMap(),
    val dismissedPermissionPrompts: Set<String> = emptySet(),
    val onboardingCompleted: Boolean = false,
) {
    companion object {
        val Default = UserPreferences()
        const val MIN_FONT_SCALE = 0.85f
        const val MAX_FONT_SCALE = 1.30f
    }
}

enum class ThemeMode { System, Light, Dark }

enum class SearchBarPosition { Top, Bottom }

interface UserPreferencesRepository {
    val preferences: StateFlow<UserPreferences>

    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}
