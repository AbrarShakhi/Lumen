package com.abrarshakhi.lumen.feature.settings.launcher

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.abrarshakhi.lumen.core.platform.app.LauncherAppsDataSource
import com.abrarshakhi.lumen.surface.launcher.LauncherRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LauncherSettingsState(
    val isOffered: Boolean = false,
    val isCurrentHome: Boolean = false,
    /** True once Lumen holds the home role, which is the only way to read app shortcuts. */
    val shortcutsAvailable: Boolean = false,
)

class LauncherSettingsViewModel(
    private val role: LauncherRole,
    private val launcherApps: LauncherAppsDataSource,
) : ViewModel() {

    private val _state = MutableStateFlow(LauncherSettingsState())
    val state: StateFlow<LauncherSettingsState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setOffered(offered: Boolean) {
        role.setEnabled(offered)
        refresh()
    }

    fun chooseHomeApp(): Intent? = role.chooseHomeAppIntent()

    fun refresh() {
        _state.value = LauncherSettingsState(
            isOffered = role.isEnabled(),
            isCurrentHome = role.isCurrentHome(),
            shortcutsAvailable = launcherApps.canReadShortcuts(),
        )
    }
}
