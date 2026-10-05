package com.abrarshakhi.lumen.provider.ai

import com.abrarshakhi.lumen.core.domain.ai.AiFailure

/**
 * User-facing AI messages, already localised.
 *
 * Follows the same pattern as the settings providers: a provider has no `Context`, and
 * `SearchResult.title` is a plain `String` on purpose — resolving resources is not a
 * provider's job. The strings are resolved once in the DI layer and handed in.
 */
data class AiMessages(
    val needsKey: String,
    val invalidKey: String,
    val accessDenied: String,
    val rateLimited: String,
    val offline: String,
    val failed: String,
) {
    fun forFailure(failure: AiFailure): String = when (failure) {
        AiFailure.MissingKey -> needsKey
        AiFailure.InvalidKey -> invalidKey
        AiFailure.AccessDenied -> accessDenied
        AiFailure.RateLimited -> rateLimited
        AiFailure.Network -> offline
        AiFailure.Unknown -> failed
    }
}
