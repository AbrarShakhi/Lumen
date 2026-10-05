package com.abrarshakhi.lumen.app

import com.abrarshakhi.lumen.app.navigation.AppRouteKey

/**
 * Decides which route an incoming launch should land on.
 *
 * Navigation 3 does not parse deep links — that is the caller's job. Lumen has several
 * entry points (launcher icon, assistant gesture, Quick Settings tile, widget, home
 * button), and every one funnels through here, so routing rules live in exactly one pure
 * function that can be unit-tested without an Activity.
 */
data class LaunchRequest(
    val route: AppRouteKey,
    val prefilledQuery: String? = null,
    val focusInput: Boolean = true,
)

/** Actions Lumen responds to, named here so the parser has no Android dependency. */
object LaunchActions {
    const val MAIN = "android.intent.action.MAIN"
    const val ASSIST = "android.intent.action.ASSIST"
    const val SEARCH = "android.intent.action.SEARCH"
    const val WEB_SEARCH = "android.intent.action.WEB_SEARCH"

    /** Extra carrying a query, matching `SearchManager.QUERY`. */
    const val EXTRA_QUERY = "query"
}

/**
 * @param action the incoming intent's action
 * @param query a query supplied by the caller, e.g. from a web-search intent
 * @param onboardingCompleted when false, everything is redirected to onboarding first
 */
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
        // A query handed to us by another app: land on search with it already typed.
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
