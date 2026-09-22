package com.abrarshakhi.lumen.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Every destination in Lumen.
 *
 * A sealed hierarchy of `NavKey`s: destinations are types, not strings, so a typo is a
 * compile error and arguments travel as real fields. `@Serializable` is what lets
 * Navigation 3 restore the back stack across process death.
 */
@Serializable
sealed interface AppRouteKey : NavKey {

    /** The search surface. The app's home, and the target of every launch surface. */
    @Serializable
    data object Search : AppRouteKey

    @Serializable
    data object Onboarding : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey

    /** Provider list: enable/disable, permission status, per-provider options. */
    @Serializable
    data object Providers : AppRouteKey

    /** Manage the keyword triggers assigned by long-pressing results. */
    @Serializable
    data object Triggers : AppRouteKey

    @Serializable
    data object SearchEngines : AppRouteKey

    @Serializable
    data object AiSettings : AppRouteKey

    /** Opt-in launcher mode. */
    @Serializable
    data object LauncherMode : AppRouteKey

    /** Folders granted through SAF for document search. */
    @Serializable
    data object FileFolders : AppRouteKey

    @Serializable
    data object Notes : AppRouteKey

    @Serializable
    data class NoteEditor(val noteId: Long? = null) : AppRouteKey
}
