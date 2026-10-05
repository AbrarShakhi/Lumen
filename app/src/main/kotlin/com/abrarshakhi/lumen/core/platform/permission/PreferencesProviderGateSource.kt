package com.abrarshakhi.lumen.core.platform.permission

import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderPreferences

/**
 * Bridges stored settings to the search engine's gate.
 *
 * Reads the current value synchronously from the preferences [kotlinx.coroutines.flow.StateFlow]
 * because gating runs on the keystroke path and must not suspend.
 */
class PreferencesProviderGateSource(
    private val preferences: UserPreferencesRepository,
) : ProviderPreferences {

    override fun isEnabled(providerId: ProviderId, defaultEnabled: Boolean): Boolean =
        preferences.preferences.value.enabledProviders[providerId.value] ?: defaultEnabled

    override fun isPermissionPromptDismissed(providerId: ProviderId): Boolean =
        providerId.value in preferences.preferences.value.dismissedPermissionPrompts
}
