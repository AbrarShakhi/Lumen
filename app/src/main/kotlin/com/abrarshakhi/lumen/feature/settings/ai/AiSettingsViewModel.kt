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

    private suspend fun refreshNow() {
        val has = secrets.has(backend.id.secretId)
        _state.update { it.copy(hasKey = has) }
    }
}
