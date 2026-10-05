package com.abrarshakhi.lumen.provider.web.di

import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.web.WebSearchProvider
import org.koin.dsl.bind
import org.koin.dsl.module

val webProviderModule = module {
    single { WebSearchProvider() } bind SearchProvider::class
}
