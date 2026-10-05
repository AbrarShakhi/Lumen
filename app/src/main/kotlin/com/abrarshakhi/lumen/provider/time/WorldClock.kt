package com.abrarshakhi.lumen.provider.time

import java.time.ZoneId

object WorldClock {

    data class Place(val query: String, val displayName: String, val zone: ZoneId)

    private val MANUAL_ALIASES: Map<String, String> = mapOf(
        "nyc" to "America/New_York",
        "new york city" to "America/New_York",
        "uk" to "Europe/London",
        "england" to "Europe/London",
        "britain" to "Europe/London",
        "usa" to "America/New_York",
        "india" to "Asia/Kolkata",
        "bangladesh" to "Asia/Dhaka",
        "japan" to "Asia/Tokyo",
        "china" to "Asia/Shanghai",
        "germany" to "Europe/Berlin",
        "france" to "Europe/Paris",
        "italy" to "Europe/Rome",
        "spain" to "Europe/Madrid",
        "russia" to "Europe/Moscow",
        "brazil" to "America/Sao_Paulo",
        "australia" to "Australia/Sydney",
        "canada" to "America/Toronto",
        "utc" to "UTC",
        "gmt" to "UTC",
    )

    private val index: Map<String, Place> by lazy { buildIndex() }

    fun find(name: String): Place? = index[name.lowercase().trim()]

    private fun buildIndex(): Map<String, Place> {
        val result = mutableMapOf<String, Place>()

        ZoneId.getAvailableZoneIds()
            .filter { it.contains('/') && !it.startsWith("SystemV/") && !it.startsWith("Etc/") }
            .forEach { id ->
                val city = id.substringAfterLast('/').replace('_', ' ')
                val key = city.lowercase()
                if (key !in result) {
                    result[key] = Place(key, city, ZoneId.of(id))
                }
            }

        MANUAL_ALIASES.forEach { (alias, zoneId) ->
            runCatching { ZoneId.of(zoneId) }.getOrNull()?.let { zone ->
                result[alias] = Place(
                    query = alias,
                    displayName = zoneId.substringAfterLast('/').replace('_', ' '),
                    zone = zone,
                )
            }
        }

        return result
    }
}
