package com.abrarshakhi.lumen.provider.math.di

import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.math.MathSearchProvider
import org.koin.dsl.bind
import org.koin.dsl.module

val mathProviderModule = module {
    single { MathSearchProvider() } bind SearchProvider::class
}
