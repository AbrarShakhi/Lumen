package com.abrarshakhi.lumen.app.di

import com.abrarshakhi.lumen.core.data.di.dataModule
import com.abrarshakhi.lumen.core.domain.di.domainModule
import com.abrarshakhi.lumen.core.platform.di.platformModule
import com.abrarshakhi.lumen.feature.search.di.searchFeatureModule
import com.abrarshakhi.lumen.feature.notes.di.notesFeatureModule
import com.abrarshakhi.lumen.feature.settings.di.settingsFeatureModule
import com.abrarshakhi.lumen.provider.ai.di.aiProviderModule
import com.abrarshakhi.lumen.provider.apps.di.appsProviderModule
import com.abrarshakhi.lumen.provider.calendar.di.calendarProviderModule
import com.abrarshakhi.lumen.provider.contacts.di.contactsProviderModule
import com.abrarshakhi.lumen.provider.files.di.filesProviderModule
import com.abrarshakhi.lumen.provider.currency.di.currencyProviderModule
import com.abrarshakhi.lumen.provider.math.di.mathProviderModule
import com.abrarshakhi.lumen.provider.notes.di.notesProviderModule
import com.abrarshakhi.lumen.provider.settings.di.settingsProviderModule
import com.abrarshakhi.lumen.provider.time.di.timeProviderModule
import com.abrarshakhi.lumen.provider.units.di.unitsProviderModule
import com.abrarshakhi.lumen.provider.web.di.webProviderModule
import org.koin.core.module.Module

/**
 * The composition root: the one place that knows every module.
 *
 * A list here is not an open/closed violation — naming every concretion is precisely a
 * composition root's job. What matters is that `SearchEngine` and `SearchViewModel` never
 * appear in it, so adding a provider adds exactly one line below and changes nothing else.
 */
object LumenModules {
    val all: List<Module> = listOf(
        platformModule,
        dataModule,
        domainModule,
        searchFeatureModule,
        settingsFeatureModule,
        notesFeatureModule,

        // Search providers — one line per capability.
        mathProviderModule,
        unitsProviderModule,
        currencyProviderModule,
        aiProviderModule,
        timeProviderModule,
        appsProviderModule,
        contactsProviderModule,
        filesProviderModule,
        calendarProviderModule,
        webProviderModule,
        notesProviderModule,
        settingsProviderModule,
    )
}
