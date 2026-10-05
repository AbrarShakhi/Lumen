package com.abrarshakhi.lumen.core.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

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
