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

/** Something the user did. The UI's vocabulary. */
interface MviIntent

/**
 * Something that happened. The reducer's vocabulary.
 *
 * Kept separate from [MviIntent] on purpose. One tap may produce several actions, and some
 * actions (a provider's results arriving) have no originating intent at all. The split is
 * what lets the reducer be a pure function, which is the single highest-value testability
 * decision in this architecture.
 */
interface MviAction

/** The complete rendering input for a screen. */
interface MviState

/** A one-shot event: navigate, launch, show a snackbar. Never part of state. */
interface MviEffect

/**
 * A pure state transition.
 *
 * No coroutines, no suspension, no dependencies — so tests read
 * `assertEquals(expected, Reducer.reduce(state, action))` with no dispatcher and no flakiness.
 */
fun interface Reducer<S : MviState, A : MviAction> {
    fun reduce(state: S, action: A): S
}

/**
 * Base for screen ViewModels.
 *
 * Responsibilities are deliberately narrow: translate intents into work, feed resulting
 * actions to the reducer, and emit effects. All decision-making about *what the new state
 * is* belongs to the reducer.
 */
abstract class MviViewModel<I : MviIntent, A : MviAction, S : MviState, E : MviEffect>(
    initialState: S,
    private val reducer: Reducer<S, A>,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    /**
     * `Channel` rather than `SharedFlow`: effects must be delivered exactly once and must
     * survive the gap while the UI is backgrounded. A `SharedFlow` with `replay = 0` drops
     * events emitted while nothing is collecting, which loses navigation and snackbars
     * across configuration changes.
     */
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
