package com.abrarshakhi.lumen.core.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Collects one-shot effects only while the screen is at least STARTED.
 *
 * `repeatOnLifecycle` matters here: navigating or launching an activity from a backgrounded
 * composition throws or silently misfires. Pairing it with the ViewModel's buffered effect
 * channel means events raised while backgrounded are delivered on return rather than lost.
 */
@Composable
fun <E> Flow<E>.CollectEffects(onEffect: suspend (E) -> Unit) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(this, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            collect { currentOnEffect(it) }
        }
    }
}
