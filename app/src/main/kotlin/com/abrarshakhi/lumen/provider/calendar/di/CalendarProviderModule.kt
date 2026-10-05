package com.abrarshakhi.lumen.provider.calendar.di

import com.abrarshakhi.lumen.core.domain.repository.CalendarRepository
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.platform.calendar.AndroidCalendarEventFormatter
import com.abrarshakhi.lumen.core.platform.calendar.CalendarDataSource
import com.abrarshakhi.lumen.core.domain.repository.CalendarEventFormatter
import com.abrarshakhi.lumen.provider.calendar.CalendarSearchProvider
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module

val calendarProviderModule = module {
    single<CalendarRepository> { CalendarDataSource(androidContext()) }
    single<CalendarEventFormatter> { AndroidCalendarEventFormatter(androidContext()) }
    single { CalendarSearchProvider(calendar = get(), formatter = get()) } bind SearchProvider::class
}
