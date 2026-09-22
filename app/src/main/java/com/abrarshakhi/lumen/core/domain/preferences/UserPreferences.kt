package com.abrarshakhi.lumen.core.domain.preferences

import kotlinx.coroutines.flow.StateFlow

/**
 * Everything the user can configure.
 *
 * A plain data class with no serialization annotations: the persisted representation lives
 * in the data layer, so this stays framework-free and `core/domain` can be lifted into a
 * pure Kotlin module without dragging a serialization dependency with it.
 *
 * Every field has a default, which is also the schema-evolution story — adding a setting
 * never needs a migration.
 */
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = true,
    val fontScale: Float = 1.0f,
    val searchBarPosition: SearchBarPosition = SearchBarPosition.Bottom,
    /** Provider id to enabled. Absent means "use the provider's declared default". */
    val enabledProviders: Map<String, Boolean> = emptyMap(),
    /** Provider ids whose permission prompt the user dismissed. */
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

/**
 * Where the query field sits.
 *
 * Defaults to [Bottom]: this is a one-handed, thumb-reachable surface, and the field is
 * the only thing the user ever touches first.
 */
enum class SearchBarPosition { Top, Bottom }

/** Reads and writes [UserPreferences]. Implemented over DataStore in the data layer. */
interface UserPreferencesRepository {
    /** Always carries a value — defaults until the first read completes. */
    val preferences: StateFlow<UserPreferences>

    suspend fun update(transform: (UserPreferences) -> UserPreferences)
}
