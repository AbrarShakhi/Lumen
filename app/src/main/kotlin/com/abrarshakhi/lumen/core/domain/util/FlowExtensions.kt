package com.abrarshakhi.lumen.core.domain.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration

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
