package com.abrarshakhi.lumen.provider.apps.di

import com.abrarshakhi.lumen.core.data.di.ApplicationScopeQualifier
import com.abrarshakhi.lumen.core.data.repository.RoomAppIndexRepository
import com.abrarshakhi.lumen.core.domain.repository.AppIndexRepository
import com.abrarshakhi.lumen.core.domain.repository.AppShortcutRepository
import com.abrarshakhi.lumen.core.platform.app.CachedAppShortcutRepository
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.platform.app.LauncherAppsDataSource
import com.abrarshakhi.lumen.provider.apps.AppsSearchProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val appsProviderModule = module {

    single { LauncherAppsDataSource(androidContext()) }

    single<AppIndexRepository> {
        RoomAppIndexRepository(
            dao = get(),
            dataSource = get(),
            scope = get(ApplicationScopeQualifier),
        )
    }

    single<AppShortcutRepository> { CachedAppShortcutRepository(get()) }

    single {
        AppsSearchProvider(index = get(), shortcuts = get())
    } bind SearchProvider::class
}
