package com.abrarshakhi.lumen.feature.search.di

import com.abrarshakhi.lumen.feature.search.ResultActionInvoker
import com.abrarshakhi.lumen.feature.search.SearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val searchFeatureModule = module {

    single { ResultActionInvoker(intentLauncher = get(), clipboard = get(), usage = get()) }

    viewModelOf(::SearchViewModel)
}
