package com.abrarshakhi.lumen.core.domain.search

/**
 * Identifies a [SearchProvider]. Stable across releases — it is persisted in user
 * preferences (per-provider enable/disable) and in usage statistics.
 */
@JvmInline
value class ProviderId(val value: String) {
    override fun toString(): String = value
}

/**
 * Identifies a single [SearchResult] within one emission.
 *
 * Conventionally namespaced by provider, e.g. `app:com.android.chrome/.Main` or
 * `contact:1234`. Used as the Compose list key, so it must be unique within a result
 * set and stable across re-queries for the same underlying item.
 */
@JvmInline
value class ResultId(val value: String) {
    override fun toString(): String = value
}
