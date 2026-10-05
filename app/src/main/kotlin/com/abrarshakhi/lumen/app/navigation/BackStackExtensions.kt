package com.abrarshakhi.lumen.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Navigation 3 hands you the back stack as an ordinary list, so "navigation" is just list
 * mutation. These name the three operations Lumen actually performs, so call sites read as
 * intent rather than as list surgery.
 *
 * Generic over [T] because `rememberNavBackStack` returns `NavBackStack<NavKey>` rather
 * than a stack parameterised by the app's own route type.
 */

/** The destination currently on screen. */
fun <T : NavKey> NavBackStack<T>.currentRoute(): T? = lastOrNull()

/** Push a destination, keeping history. */
fun <T : NavKey> NavBackStack<T>.navigateTo(route: T) {
    add(route)
}

/**
 * Replace the entire stack with [route].
 *
 * For switching top-level surfaces, e.g. leaving onboarding for search — going "back" into
 * onboarding afterwards would make no sense.
 */
fun <T : NavKey> NavBackStack<T>.switchTopTo(route: T) {
    clear()
    add(route)
}

/**
 * Pop one entry, returning false when already at the root.
 *
 * The caller decides what an unhandled back means: on the search surface it finishes the
 * activity so Lumen behaves like a dismissible overlay rather than trapping the user.
 */
fun <T : NavKey> NavBackStack<T>.popOrFalse(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}
