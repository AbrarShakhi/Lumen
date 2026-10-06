package com.abrarshakhi.lumen.surface.di

import com.abrarshakhi.lumen.core.domain.platform.HomeScreenSurfaces
import com.abrarshakhi.lumen.surface.AndroidHomeScreenSurfaces
import com.abrarshakhi.lumen.surface.widget.NotesWidgetUpdater
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val surfaceModule = module {
    single<HomeScreenSurfaces> { AndroidHomeScreenSurfaces(androidContext()) }
    single { NotesWidgetUpdater(androidContext(), notes = get()) }
}
