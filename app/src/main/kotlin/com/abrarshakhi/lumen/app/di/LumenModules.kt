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
import com.abrarshakhi.lumen.surface.di.surfaceModule
import org.koin.core.module.Module

object LumenModules {
    val all: List<Module> = listOf(
        platformModule,
        dataModule,
        domainModule,
        searchFeatureModule,
        settingsFeatureModule,
        notesFeatureModule,
        surfaceModule,

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
