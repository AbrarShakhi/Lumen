package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

/**
 * A system settings screen Lumen can open.
 *
 * [title] arrives already localised: resolving it belongs to the platform layer, which has
 * a `Context`, so the provider that searches these never needs one.
 *
 * [keywords] carry the words people actually type — "wifi" for "Wi-Fi", "ringtone" for
 * "Sound & vibration" — which a name-only match would miss.
 */
data class SystemSettingEntry(
    val id: String,
    val title: String,
    val action: String,
    val searchable: SearchableText,
    val keywords: List<SearchableText>,
)

/**
 * The settings screens available on *this* device.
 *
 * Availability matters: OEM builds omit screens (and ColorOS renames or hides several), so
 * every entry is probed before being offered rather than failing when tapped.
 */
interface SystemSettingsRepository {
    suspend fun entries(): List<SystemSettingEntry>
}

/**
 * One of Lumen's own settings, reachable from search like anything else.
 *
 * Mirrors [SystemSettingEntry]: the title is resolved by the platform layer, so the
 * provider that searches these stays free of `Context` and of navigation types.
 */
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
