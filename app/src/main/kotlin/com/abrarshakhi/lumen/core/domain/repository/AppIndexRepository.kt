package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText
import kotlinx.coroutines.flow.StateFlow

data class IndexedApp(
    val packageName: String,
    val activityName: String,
    val label: String,
    val searchable: SearchableText,
    val isSystem: Boolean,
) {
    val componentKey: String get() = "$packageName/$activityName"
}

interface AppIndexRepository {
    val apps: StateFlow<List<IndexedApp>>

    suspend fun refresh()
}
