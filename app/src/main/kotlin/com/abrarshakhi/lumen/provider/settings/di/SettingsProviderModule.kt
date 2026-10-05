package com.abrarshakhi.lumen.provider.settings.di

import com.abrarshakhi.lumen.core.domain.repository.LumenSettingsRepository
import com.abrarshakhi.lumen.core.domain.repository.SystemSettingsRepository
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.platform.settings.AndroidLumenSettingsRepository
import com.abrarshakhi.lumen.core.platform.settings.AndroidSystemSettingsRepository
import com.abrarshakhi.lumen.provider.settings.LumenSettingsProvider
import com.abrarshakhi.lumen.provider.settings.SystemSettingsProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val settingsProviderModule = module {
    single<SystemSettingsRepository> { AndroidSystemSettingsRepository(androidContext()) }
    single<LumenSettingsRepository> { AndroidLumenSettingsRepository(androidContext()) }

    single { SystemSettingsProvider(settings = get()) } bind SearchProvider::class
    single { LumenSettingsProvider(settings = get()) } bind SearchProvider::class
}
