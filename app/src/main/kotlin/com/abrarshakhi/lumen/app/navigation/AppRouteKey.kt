package com.abrarshakhi.lumen.app.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRouteKey : NavKey {

    @Serializable
    data object Search : AppRouteKey

    @Serializable
    data object Onboarding : AppRouteKey

    @Serializable
    data object Settings : AppRouteKey

    @Serializable
    data object Providers : AppRouteKey

    @Serializable
    data object Triggers : AppRouteKey

    @Serializable
    data object SearchEngines : AppRouteKey

    @Serializable
    data object AiSettings : AppRouteKey

    @Serializable
    data object LauncherMode : AppRouteKey

    @Serializable
    data object FileFolders : AppRouteKey

    @Serializable
    data object Notes : AppRouteKey

    @Serializable
    data class NoteEditor(val noteId: Long? = null) : AppRouteKey
}
