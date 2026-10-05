package com.abrarshakhi.lumen.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

fun <T : NavKey> NavBackStack<T>.currentRoute(): T? = lastOrNull()

fun <T : NavKey> NavBackStack<T>.navigateTo(route: T) {
    add(route)
}

fun <T : NavKey> NavBackStack<T>.switchTopTo(route: T) {
    clear()
    add(route)
}

fun <T : NavKey> NavBackStack<T>.popOrFalse(): Boolean {
    if (size <= 1) return false
    removeAt(lastIndex)
    return true
}
