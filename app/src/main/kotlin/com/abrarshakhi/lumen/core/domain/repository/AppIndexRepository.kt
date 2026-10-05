package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText
import kotlinx.coroutines.flow.StateFlow

/**
 * One launchable activity, with its match forms already computed.
 *
 * [searchable] is built at index time, not per keystroke — normalising a few hundred
 * labels on every character typed is exactly what makes a search field feel sluggish.
 */
data class IndexedApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val searchable: SearchableText,
    val isSystem: Boolean,
) {
    /** Stable identity for both result ids and usage ranking. */
    val componentKey: String get() = "$packageName/$activityName"
}

/**
 * The installed-app index.
 *
 * Exposed as a hot [StateFlow] so searching is a pure in-memory filter with no I/O and no
 * PackageManager round-trip on the keystroke path.
 */
interface AppIndexRepository {
    val apps: StateFlow<List<IndexedApp>>

    /** Rescans installed apps and updates the index. Safe to call repeatedly. */
    suspend fun refresh()
}
