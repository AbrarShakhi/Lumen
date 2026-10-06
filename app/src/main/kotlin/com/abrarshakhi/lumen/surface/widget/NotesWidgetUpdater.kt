package com.abrarshakhi.lumen.surface.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds

class NotesWidgetUpdater(
    private val context: Context,
    private val notes: NoteRepository,
) {

    @OptIn(FlowPreview::class)
    fun start(scope: CoroutineScope) {
        notes.observeAll()
            .distinctUntilChanged()
            .drop(1)
            .debounce(REFRESH_DELAY)
            .onEach { runCatching { LumenNotesWidget().updateAll(context) } }
            .launchIn(scope)
    }

    private companion object {
        val REFRESH_DELAY = 600.milliseconds
    }
}
