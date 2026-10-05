package com.abrarshakhi.lumen.core.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

/**
 * Stops collecting [this] once [duration] has elapsed since collection began, keeping
 * whatever was already emitted and completing normally.
 *
 * Deliberately not `kotlinx.coroutines.flow.timeout`: that measures the gap *between*
 * emissions and cancels the flow with an exception. A streaming provider whose first token
 * is slow would be killed by a gap-based timeout even though it is working correctly. What
 * the search engine wants is a total budget — "you have 800ms, and I keep whatever you
 * produced within it" — which is what this does.
 */
fun <T> Flow<T>.takeUntilTimeout(duration: Duration): Flow<T> =
    if (duration == Duration.INFINITE) {
        this
    } else {
        channelFlow {
            val collector = launch {
                collect { send(it) }
            }
            launch {
                delay(duration)
                collector.cancel()
            }
            collector.join()
        }
    }
