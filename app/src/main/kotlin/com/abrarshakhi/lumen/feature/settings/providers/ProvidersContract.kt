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

sealed interface ProviderPermissionState {
    data object NotRequired : ProviderPermissionState

    data object Granted : ProviderPermissionState

    data class Missing(val permissions: List<AppPermission>) : ProviderPermissionState

    data class Blocked(val permissions: List<AppPermission>) : ProviderPermissionState
}

sealed interface ProvidersEffect : MviEffect {
    data class RequestPermissions(val permissions: List<AppPermission>) : ProvidersEffect
    data object OpenAppSettings : ProvidersEffect
}
