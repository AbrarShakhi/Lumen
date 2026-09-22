package com.abrarshakhi.lumen.core.platform.di

import android.content.Context
import com.abrarshakhi.lumen.core.domain.permission.CapabilityChecker
import com.abrarshakhi.lumen.core.domain.permission.PermissionChecker
import com.abrarshakhi.lumen.core.domain.permission.PermissionRequestRecorder
import com.abrarshakhi.lumen.core.domain.platform.ClipboardWriter
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.search.ProviderPreferences
import com.abrarshakhi.lumen.core.platform.intent.AndroidClipboardWriter
import com.abrarshakhi.lumen.core.platform.intent.AndroidIntentLauncher
import com.abrarshakhi.lumen.core.platform.permission.AndroidCapabilityChecker
import com.abrarshakhi.lumen.core.platform.permission.AndroidPermissionChecker
import com.abrarshakhi.lumen.core.platform.permission.PreferencesProviderGateSource
import com.abrarshakhi.lumen.surface.launcher.LauncherRole
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val platformModule = module {

    single<IntentLauncher> { AndroidIntentLauncher(androidContext(), get()) }

    single<ClipboardWriter> { AndroidClipboardWriter(androidContext()) }

    single {
        AndroidPermissionChecker(
            context = androidContext(),
            askHistory = androidContext().getSharedPreferences(
                AndroidPermissionChecker.PREFS_NAME,
                Context.MODE_PRIVATE,
            ),
        )
    }
    single<PermissionChecker> { get<AndroidPermissionChecker>() }
    single<PermissionRequestRecorder> { get<AndroidPermissionChecker>() }

    single<CapabilityChecker> { AndroidCapabilityChecker(androidContext()) }

    single<ProviderPreferences> { PreferencesProviderGateSource(get()) }

    single { LauncherRole(androidContext()) }
}
