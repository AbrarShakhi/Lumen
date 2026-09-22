package com.abrarshakhi.lumen.feature.settings.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.abrarshakhi.lumen.core.domain.ai.AiBackend
import com.abrarshakhi.lumen.core.domain.secret.SecretStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Manages the stored AI key.
 *
 * Plain [ViewModel] rather than the MVI base: there is no reducer worth having for a screen
 * whose entire state is "is a key set", and forcing the ceremony would add indirection
 * without adding clarity.
 */
class AiSettingsViewModel(
    private val backend: AiBackend,
    private val secrets: SecretStore,
) : ViewModel() {

    private val _state = MutableStateFlow(AiSettingsState(backendName = backend.displayName))
    val state: StateFlow<AiSettingsState> = _state.asStateFlow()

    val keyUrl: String get() = backend.keyUrl

    init {
        refresh()
    }

    fun save(key: String) {
        val trimmed = key.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            secrets.put(backend.id.secretId, trimmed)
            refreshNow()
        }
    }

    fun remove() {
        viewModelScope.launch {
            secrets.clear(backend.id.secretId)
            refreshNow()
        }
    }

    private fun refresh() {
        viewModelScope.launch { refreshNow() }
    }

    /** Only ever asks *whether* a key exists — the value is never read back into the UI. */
    private suspend fun refreshNow() {
        val has = secrets.has(backend.id.secretId)
        _state.update { it.copy(hasKey = has) }
    }
}
