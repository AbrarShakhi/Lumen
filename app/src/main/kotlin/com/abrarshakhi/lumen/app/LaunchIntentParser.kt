package com.abrarshakhi.lumen.app

import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.app.navigation.AppRouteKey

data class LaunchRequest(
    val route: AppRouteKey,
    val prefilledQuery: String? = null,
    val focusInput: Boolean = true,
)

@Immutable
data class LaunchEvent(
    val request: LaunchRequest,
    val serial: Int = 0,
)

object LaunchActions {
    const val MAIN = "android.intent.action.MAIN"
    const val ASSIST = "android.intent.action.ASSIST"
    const val SEARCH = "android.intent.action.SEARCH"
    const val WEB_SEARCH = "android.intent.action.WEB_SEARCH"
    const val OPEN_NOTES = "com.abrarshakhi.lumen.action.OPEN_NOTES"
    const val NEW_NOTE = "com.abrarshakhi.lumen.action.NEW_NOTE"
    const val OPEN_NOTE = "com.abrarshakhi.lumen.action.OPEN_NOTE"
    const val OPEN_SETTINGS = "com.abrarshakhi.lumen.action.OPEN_SETTINGS"

    const val EXTRA_QUERY = "query"
    const val EXTRA_NOTE_ID = "note_id"
}

fun parseLaunchIntent(
    action: String?,
    query: String? = null,
    noteId: Long? = null,
    onboardingCompleted: Boolean = true,
): LaunchRequest {
    if (!onboardingCompleted) {
        return LaunchRequest(route = AppRouteKey.Onboarding, focusInput = false)
    }

    val trimmedQuery = query?.trim()?.takeIf { it.isNotEmpty() }

    return when (action) {
        LaunchActions.SEARCH, LaunchActions.WEB_SEARCH -> LaunchRequest(
            route = AppRouteKey.Search,
            prefilledQuery = trimmedQuery,
            focusInput = trimmedQuery == null,
        )

        LaunchActions.OPEN_NOTES -> LaunchRequest(route = AppRouteKey.Notes, focusInput = false)

        LaunchActions.NEW_NOTE -> LaunchRequest(
            route = AppRouteKey.NoteEditor(noteId = null),
            focusInput = false,
        )

        LaunchActions.OPEN_NOTE -> LaunchRequest(
            route = noteId?.let { AppRouteKey.NoteEditor(noteId = it) } ?: AppRouteKey.Notes,
            focusInput = false,
        )

        LaunchActions.OPEN_SETTINGS -> LaunchRequest(route = AppRouteKey.Settings, focusInput = false)

        else -> LaunchRequest(
            route = AppRouteKey.Search,
            prefilledQuery = trimmedQuery,
            focusInput = true,
        )
    }
}
