package com.abrarshakhi.lumen.feature.settings.providers

import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.text.TextValue
import com.abrarshakhi.lumen.core.mvi.MviAction
import com.abrarshakhi.lumen.core.mvi.MviEffect
import com.abrarshakhi.lumen.core.mvi.MviIntent
import com.abrarshakhi.lumen.core.mvi.MviState

sealed interface ProvidersIntent : MviIntent {
    data class EnabledSet(val providerId: ProviderId, val enabled: Boolean) : ProvidersIntent
    data class PermissionRequested(val providerId: ProviderId) : ProvidersIntent

    /**
     * Re-read permission state.
     *
     * Permissions can change outside the app — in system settings, or via the OS dialog —
     * and there is no flow to observe, so the screen re-reads whenever it resumes.
     */
    data object Refreshed : ProvidersIntent
}

sealed interface ProvidersAction : MviAction {
    data class ProvidersLoaded(val providers: List<ProviderSetting>) : ProvidersAction
}

@Immutable
data class ProvidersState(
    val providers: List<ProviderSetting> = emptyList(),
) : MviState

@Immutable
data class ProviderSetting(
    val id: ProviderId,
    val name: TextValue,
    val enabled: Boolean,
    val permission: ProviderPermissionState,
)

/** What is standing between a provider and being able to produce results. */
sealed interface ProviderPermissionState {
    /** The provider needs nothing granted. */
    data object NotRequired : ProviderPermissionState

    data object Granted : ProviderPermissionState

    /** Missing, but still requestable with a dialog. */
    data class Missing(val permissions: List<AppPermission>) : ProviderPermissionState

    /**
     * Denied with "don't ask again" — a dialog would be a no-op, so the UI must send the
     * user to system settings instead of pretending a prompt will work.
     */
    data class Blocked(val permissions: List<AppPermission>) : ProviderPermissionState
}

sealed interface ProvidersEffect : MviEffect {
    data class RequestPermissions(val permissions: List<AppPermission>) : ProvidersEffect
    data object OpenAppSettings : ProvidersEffect
}
