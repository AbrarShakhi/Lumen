package com.abrarshakhi.lumen.feature.notes

import androidx.compose.runtime.Immutable
import com.abrarshakhi.lumen.core.domain.repository.Note
import com.abrarshakhi.lumen.core.mvi.MviAction
import com.abrarshakhi.lumen.core.mvi.MviEffect
import com.abrarshakhi.lumen.core.mvi.MviIntent
import com.abrarshakhi.lumen.core.mvi.MviState

sealed interface NotesIntent : MviIntent {
    data class Deleted(val id: Long) : NotesIntent
}

sealed interface NotesAction : MviAction {
    data class NotesLoaded(val notes: List<Note>) : NotesAction
}

@Immutable
data class NotesState(val notes: List<Note> = emptyList()) : MviState {
    val isEmpty: Boolean get() = notes.isEmpty()
}

sealed interface NotesEffect : MviEffect

// --- Editor ---------------------------------------------------------------------------

sealed interface NoteEditorIntent : MviIntent {
    data class TitleChanged(val value: String) : NoteEditorIntent
    data class BodyChanged(val value: String) : NoteEditorIntent
    data object Deleted : NoteEditorIntent
    data object Closed : NoteEditorIntent
}

sealed interface NoteEditorAction : MviAction {
    data class Loaded(val title: String, val body: String) : NoteEditorAction
    data class TitleChanged(val value: String) : NoteEditorAction
    data class BodyChanged(val value: String) : NoteEditorAction
}

@Immutable
data class NoteEditorState(
    val title: String = "",
    val body: String = "",
) : MviState {
    val isBlank: Boolean get() = title.isBlank() && body.isBlank()
}

sealed interface NoteEditorEffect : MviEffect {
    data object Close : NoteEditorEffect
}
