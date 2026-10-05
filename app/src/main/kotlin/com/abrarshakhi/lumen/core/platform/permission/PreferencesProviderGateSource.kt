package com.abrarshakhi.lumen.core.platform.permission

import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderPreferences

class PreferencesProviderGateSource(
    private val preferences: UserPreferencesRepository,
) : ProviderPreferences {

    override fun isEnabled(providerId: ProviderId, defaultEnabled: Boolean): Boolean =
        preferences.preferences.value.enabledProviders[providerId.value] ?: defaultEnabled

    override fun isPermissionPromptDismissed(providerId: ProviderId): Boolean =
        providerId.value in preferences.preferences.value.dismissedPermissionPrompts
}
