package com.abrarshakhi.lumen.feature.settings.di

import com.abrarshakhi.lumen.feature.settings.SettingsViewModel
import com.abrarshakhi.lumen.feature.settings.ai.AiSettingsViewModel
import com.abrarshakhi.lumen.feature.settings.files.FileFoldersViewModel
import com.abrarshakhi.lumen.feature.settings.launcher.LauncherSettingsViewModel
import com.abrarshakhi.lumen.feature.settings.providers.ProvidersViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsFeatureModule = module {
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProvidersViewModel)
    viewModelOf(::AiSettingsViewModel)
    viewModelOf(::FileFoldersViewModel)
    viewModelOf(::LauncherSettingsViewModel)
}
