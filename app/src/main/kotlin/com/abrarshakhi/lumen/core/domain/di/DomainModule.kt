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

val domainModule = module {

    single<SearchProviderRegistry> {
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
