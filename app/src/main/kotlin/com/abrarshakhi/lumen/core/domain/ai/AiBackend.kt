package com.abrarshakhi.lumen.core.domain.ai

import com.abrarshakhi.lumen.core.domain.secret.SecretId

/** Identifies an AI provider Lumen can ask. */
@JvmInline
value class AiBackendId(val value: String) {
    override fun toString(): String = value

    val secretId: SecretId get() = SecretId.forAiBackend(value)
}

/** What an answer attempt produced. */
sealed interface AiAnswer {
    data class Text(val content: String) : AiAnswer

    /** A failure worth telling the user about — a bad key, a rate limit, no network. */
    data class Failed(val reason: AiFailure) : AiAnswer
}

enum class AiFailure {
    MissingKey,

    /** The key itself was not accepted — regenerating it is the fix. */
    InvalidKey,

    /**
     * The key is well-formed but its project is not permitted to use the API.
     *
     * Kept distinct from [InvalidKey] on purpose: reporting this as a bad key sends the
     * user off to regenerate a perfectly good one, when the actual fix is on the account.
     */
    AccessDenied,

    RateLimited,
    Network,
    Unknown,
}

/**
 * One AI service.
 *
 * Every backend is reached through this interface so the provider that uses them has a
 * single code path: adding OpenAI or Claude later is a new implementation plus a line of
 * DI, not a branch in the provider.
 *
 * The API key is passed in rather than read here — backends stay free of storage concerns,
 * and the key never sits in more places than it must.
 */
interface AiBackend {
    val id: AiBackendId

    /** Shown in settings, e.g. "Gemini". */
    val displayName: String

    /** Where the user obtains a key, shown alongside the key field. */
    val keyUrl: String

    suspend fun answer(prompt: String, apiKey: String): AiAnswer
}
