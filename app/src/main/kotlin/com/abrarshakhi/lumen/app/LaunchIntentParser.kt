package com.abrarshakhi.lumen.app

import com.abrarshakhi.lumen.app.navigation.AppRouteKey

data class LaunchRequest(
    val route: AppRouteKey,
    val prefilledQuery: String? = null,
    val focusInput: Boolean = true,
)

object LaunchActions {
    const val MAIN = "android.intent.action.MAIN"
    const val ASSIST = "android.intent.action.ASSIST"
    const val SEARCH = "android.intent.action.SEARCH"
    const val WEB_SEARCH = "android.intent.action.WEB_SEARCH"

    const val EXTRA_QUERY = "query"
}

fun parseLaunchIntent(
    action: String?,
    query: String? = null,
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

        else -> LaunchRequest(
            route = AppRouteKey.Search,
            prefilledQuery = trimmedQuery,
            focusInput = true,
        )
    }
}
