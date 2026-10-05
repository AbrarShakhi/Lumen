package com.abrarshakhi.lumen.core.domain.platform

interface IntentLauncher {
    suspend fun launch(intent: PlatformIntent): Boolean
}

interface ClipboardWriter {
    suspend fun copy(label: String, text: String)
}
