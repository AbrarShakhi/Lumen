package com.abrarshakhi.lumen.core.domain.ai

import com.abrarshakhi.lumen.core.domain.secret.SecretId

@JvmInline
value class AiBackendId(val value: String) {
    override fun toString(): String = value

    val secretId: SecretId get() = SecretId.forAiBackend(value)
}

sealed interface AiAnswer {
    data class Text(val content: String) : AiAnswer

    data class Failed(val reason: AiFailure) : AiAnswer
}

enum class AiFailure {
    MissingKey,

    InvalidKey,

    AccessDenied,

    RateLimited,
    Network,
    Unknown,
}

interface AiBackend {
    val id: AiBackendId

    val displayName: String

    val keyUrl: String

    suspend fun answer(prompt: String, apiKey: String): AiAnswer
}
