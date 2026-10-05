package com.abrarshakhi.lumen.provider.ai

import com.abrarshakhi.lumen.core.domain.ai.AiFailure

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
