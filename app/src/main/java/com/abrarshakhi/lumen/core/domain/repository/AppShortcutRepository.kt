package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

/** A shortcut published by an installed app, with its match form precomputed. */
data class IndexedShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val appLabel: String,
    val searchable: SearchableText,
)

/**
 * Shortcuts published by installed apps.
 *
 * Always empty unless Lumen holds the home role — Android shares shortcuts only with the
 * current launcher. The repository exists so the provider can ask without knowing that, and
 * degrade silently to plain app results.
 */
interface AppShortcutRepository {
    suspend fun shortcuts(): List<IndexedShortcut>

    /** Drops the cache, e.g. after the home role changes. */
    fun invalidate()
}
