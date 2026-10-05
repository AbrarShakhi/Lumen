package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

data class IndexedShortcut(
    val id: String,
    val packageName: String,
    val label: String,
    val appLabel: String,
    val searchable: SearchableText,
)

interface AppShortcutRepository {
    suspend fun shortcuts(): List<IndexedShortcut>

    fun invalidate()
}
