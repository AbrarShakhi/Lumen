package com.abrarshakhi.lumen.core.platform.intent

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.getSystemService
import com.abrarshakhi.lumen.core.domain.platform.ClipboardWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidClipboardWriter(private val context: Context) : ClipboardWriter {

    override suspend fun copy(label: String, text: String) {
        withContext(Dispatchers.Main.immediate) {
            context.getSystemService<ClipboardManager>()
                ?.setPrimaryClip(ClipData.newPlainText(label, text))
        }
    }
}
