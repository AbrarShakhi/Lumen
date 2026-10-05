package com.abrarshakhi.lumen.feature.settings.providers

import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.PermissionChecker
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.domain.search.SearchProviderRegistry
import com.abrarshakhi.lumen.core.mvi.MviViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Lists every registered provider with its enabled and permission state.
 *
 * Reads from the registry rather than a hard-coded list, so a newly added provider appears
 * here automatically — the same property that keeps the search engine closed to modification.
 */
class ProvidersViewModel(
    private val registry: SearchProviderRegistry,
    private val permissions: PermissionChecker,
    private val preferences: UserPreferencesRepository,
) : MviViewModel<ProvidersIntent, ProvidersAction, ProvidersState, ProvidersEffect>(
    initialState = ProvidersState(),
    reducer = ProvidersReducer,
) {

    init {
        preferences.preferences
            .onEach { reduce(ProvidersAction.ProvidersLoaded(buildSettings())) }
            .launchIn(viewModelScope)
    }

    override suspend fun handleIntent(intent: ProvidersIntent) {
        when (intent) {
            is ProvidersIntent.EnabledSet -> preferences.update { current ->
                current.copy(
                    enabledProviders = current.enabledProviders +
                        (intent.providerId.value to intent.enabled),
                )
            }

            is ProvidersIntent.PermissionRequested -> {
                val setting = currentState.providers
                    .firstOrNull { it.id == intent.providerId } ?: return
                when (val permission = setting.permission) {
                    is ProviderPermissionState.Missing ->
                        emitEffect(ProvidersEffect.RequestPermissions(permission.permissions))
                    // A prompt would be dismissed instantly; system settings is the only route.
                    is ProviderPermissionState.Blocked ->
                        emitEffect(ProvidersEffect.OpenAppSettings)
                    else -> Unit
                }
            }

            ProvidersIntent.Refreshed -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch { reduce(ProvidersAction.ProvidersLoaded(buildSettings())) }
    }

    private fun buildSettings(): List<ProviderSetting> {
        val stored = preferences.preferences.value.enabledProviders
        return registry.providers.map { provider ->
            ProviderSetting(
                id = provider.id,
                name = provider.metadata.displayName,
                enabled = stored[provider.id.value] ?: provider.metadata.defaultEnabled,
                permission = provider.permissionState(),
            )
        }
    }

    private fun SearchProvider.permissionState(): ProviderPermissionState {
        // Only the permissions that apply to this device's SDK level are relevant; asking
        // for the others would be asking for something the platform will never grant.
        val required = AppPermission.applicable(
            metadata.requiredPermissions,
            permissions.sdkInt,
        )
        if (required.isEmpty()) return ProviderPermissionState.NotRequired

        val missing = required.filterNot { permissions.isGranted(it) }
        return when {
            missing.isEmpty() -> ProviderPermissionState.Granted
            missing.any { permissions.isPermanentlyDenied(it) } ->
                ProviderPermissionState.Blocked(missing)
            else -> ProviderPermissionState.Missing(missing)
        }
    }
}
