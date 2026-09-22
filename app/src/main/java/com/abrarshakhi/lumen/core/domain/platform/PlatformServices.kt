package com.abrarshakhi.lumen.core.domain.platform

/**
 * Launches a [PlatformIntent]. Implemented over Android intents in the platform layer.
 *
 * Returns false when nothing on the device can handle the request, so callers can report
 * that rather than swallowing an ActivityNotFoundException.
 */
interface IntentLauncher {
    suspend fun launch(intent: PlatformIntent): Boolean
}

/** Writes to the system clipboard. */
interface ClipboardWriter {
    suspend fun copy(label: String, text: String)
}
