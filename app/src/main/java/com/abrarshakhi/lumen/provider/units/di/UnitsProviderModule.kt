package com.abrarshakhi.lumen.provider.units.di

import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.units.UnitConversionProvider
import org.koin.dsl.bind
import org.koin.dsl.module

val unitsProviderModule = module {
    single { UnitConversionProvider() } bind SearchProvider::class
}
