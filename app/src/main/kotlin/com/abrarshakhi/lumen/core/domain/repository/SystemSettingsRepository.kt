package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

data class SystemSettingEntry(
    val id: String,
    val title: String,
    val action: String,
    val searchable: SearchableText,
    val keywords: List<SearchableText>,
)

interface SystemSettingsRepository {
    suspend fun entries(): List<SystemSettingEntry>
}

data class LumenSettingEntry(
    val id: String,
    val title: String,
    val destination: com.abrarshakhi.lumen.core.domain.search.InternalDestination,
    val searchable: SearchableText,
    val keywords: List<SearchableText>,
)

interface LumenSettingsRepository {
    fun entries(): List<LumenSettingEntry>
}
