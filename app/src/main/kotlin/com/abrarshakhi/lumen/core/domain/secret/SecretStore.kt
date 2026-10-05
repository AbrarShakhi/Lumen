package com.abrarshakhi.lumen.core.domain.secret

/** Identifies a stored secret. Stable — it is the persistence key. */
@JvmInline
value class SecretId(val value: String) {
    override fun toString(): String = value

    companion object {
        fun forAiBackend(backendId: String) = SecretId("ai.$backendId")
    }
}

/**
 * Encrypted storage for values Lumen must keep but must never expose — API keys.
 *
 * Deliberately narrow: no listing, no bulk read. A caller can only ask for a key it already
 * knows the id of, which keeps accidental exposure surfaces small.
 */
interface SecretStore {
    suspend fun put(id: SecretId, value: String)

    suspend fun get(id: SecretId): String?

    suspend fun clear(id: SecretId)

    /** True when a secret exists, for UI that shows "configured" without reading the value. */
    suspend fun has(id: SecretId): Boolean = get(id) != null
}
