package com.abrarshakhi.lumen.provider.currency.di

import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.provider.currency.CurrencyConversionProvider
import com.abrarshakhi.lumen.provider.currency.CurrencyRatesRepository
import com.abrarshakhi.lumen.provider.currency.ExchangeRatesClient
import org.koin.dsl.bind
import org.koin.dsl.module

val currencyProviderModule = module {
    single { ExchangeRatesClient(client = get()) }
    single { CurrencyRatesRepository(client = get()) }
    single { CurrencyConversionProvider(rates = get()) } bind SearchProvider::class
}
