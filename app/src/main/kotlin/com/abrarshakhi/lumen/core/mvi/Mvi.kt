package com.abrarshakhi.lumen.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface MviIntent

interface MviAction

interface MviState

interface MviEffect

fun interface Reducer<S : MviState, A : MviAction> {
    fun reduce(state: S, action: A): S
}

abstract class MviViewModel<I : MviIntent, A : MviAction, S : MviState, E : MviEffect>(
    initialState: S,
    private val reducer: Reducer<S, A>,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects: Flow<E> = _effects.receiveAsFlow()

    protected val currentState: S get() = _state.value

    fun dispatch(intent: I) {
        viewModelScope.launch { handleIntent(intent) }
    }

    protected abstract suspend fun handleIntent(intent: I)

    protected fun reduce(action: A) {
        _state.update { reducer.reduce(it, action) }
    }

    protected suspend fun emitEffect(effect: E) {
        _effects.send(effect)
    }
}
