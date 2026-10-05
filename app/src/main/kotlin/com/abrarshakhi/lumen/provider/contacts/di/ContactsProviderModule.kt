package com.abrarshakhi.lumen.provider.contacts.di

import com.abrarshakhi.lumen.core.domain.repository.ContactRepository
import com.abrarshakhi.lumen.core.domain.repository.InstalledAppsProbe
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.platform.app.AndroidInstalledAppsProbe
import com.abrarshakhi.lumen.core.platform.contacts.CachedContactRepository
import com.abrarshakhi.lumen.core.platform.contacts.ContactsDataSource
import com.abrarshakhi.lumen.provider.contacts.ContactsSearchProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val contactsProviderModule = module {

    single { ContactsDataSource(androidContext()) }

    single<ContactRepository> {
        val dataSource: ContactsDataSource = get()
        CachedContactRepository(load = dataSource::loadContacts)
    }

    single { AndroidInstalledAppsProbe(androidContext()) }
    single<InstalledAppsProbe> { get<AndroidInstalledAppsProbe>() }

    single {
        ContactsSearchProvider(contacts = get(), installedApps = get())
    } bind SearchProvider::class
}
