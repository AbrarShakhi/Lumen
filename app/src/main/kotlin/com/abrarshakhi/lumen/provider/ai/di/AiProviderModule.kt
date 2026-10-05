package com.abrarshakhi.lumen.provider.ai.di

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.ai.AiBackend
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.ai.AiAnswerProvider
import com.abrarshakhi.lumen.provider.ai.AiMessages
import com.abrarshakhi.lumen.provider.ai.GeminiBackend
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val aiProviderModule = module {

    // One backend today. Adding OpenAI or Claude means another implementation bound here
    // and a way to choose between them — the provider itself does not change.
    single<AiBackend> { GeminiBackend(client = get()) }

    single {
        val context = androidContext()
        AiMessages(
            needsKey = context.getString(R.string.ai_needs_key),
            invalidKey = context.getString(R.string.ai_invalid_key),
            accessDenied = context.getString(R.string.ai_access_denied),
            rateLimited = context.getString(R.string.ai_rate_limited),
            offline = context.getString(R.string.ai_offline),
            failed = context.getString(R.string.ai_failed),
        )
    }

    single {
        AiAnswerProvider(backend = get(), secrets = get(), messages = get())
    } bind SearchProvider::class
}
