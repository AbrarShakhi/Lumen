package com.abrarshakhi.lumen.core.domain.di

import com.abrarshakhi.lumen.core.domain.rank.DefaultResultRanker
import com.abrarshakhi.lumen.core.domain.rank.RankingWeights
import com.abrarshakhi.lumen.core.domain.rank.ResultRanker
import com.abrarshakhi.lumen.core.domain.repository.UsageRepository
import com.abrarshakhi.lumen.core.domain.search.DefaultProviderGate
import com.abrarshakhi.lumen.core.domain.search.DefaultSearchProviderRegistry
import com.abrarshakhi.lumen.core.domain.search.DefaultTriggerResolver
import com.abrarshakhi.lumen.core.domain.search.ProviderGate
import com.abrarshakhi.lumen.core.domain.search.SearchEngine
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.domain.search.SearchProviderRegistry
import com.abrarshakhi.lumen.core.domain.search.TriggerResolver
import org.koin.dsl.module

/**
 * Wiring for the search domain.
 *
 * The registry collects providers with `getAll`, so a new provider is registered by its own
 * module and picked up here without this file changing. Ordering comes from each provider's
 * declared metadata, never from the order Koin happens to return them in.
 */
val domainModule = module {

    single<SearchProviderRegistry> {
        // Note: this resolves every SearchProvider eagerly, which is why provider
        // constructors must stay trivial — real work belongs in warmUp()/search().
        DefaultSearchProviderRegistry(getKoin().getAll<SearchProvider>())
    }

    single<ResultRanker> { DefaultResultRanker(RankingWeights.Default) }

    single<TriggerResolver> { DefaultTriggerResolver() }

    single<ProviderGate> { DefaultProviderGate(get(), get(), get()) }

    single {
        SearchEngine(
            registry = get(),
            ranker = get(),
            triggers = get(),
            gate = get(),
            usage = get<UsageRepository>().snapshot,
        )
    }
}
