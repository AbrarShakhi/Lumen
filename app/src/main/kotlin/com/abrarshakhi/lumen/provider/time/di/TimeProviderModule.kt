package com.abrarshakhi.lumen.provider.time.di

import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.time.TimeSearchProvider
import org.koin.dsl.bind
import org.koin.dsl.module

val timeProviderModule = module {
    single { TimeSearchProvider() } bind SearchProvider::class
}
