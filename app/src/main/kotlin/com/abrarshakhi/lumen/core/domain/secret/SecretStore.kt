package com.abrarshakhi.lumen.core.domain.secret

@JvmInline
value class SecretId(val value: String) {
    override fun toString(): String = value

    companion object {
        fun forAiBackend(backendId: String) = SecretId("ai.$backendId")
    }
}

interface SecretStore {
    suspend fun put(id: SecretId, value: String)

    suspend fun get(id: SecretId): String?

    suspend fun clear(id: SecretId)

    suspend fun has(id: SecretId): Boolean = get(id) != null
}
