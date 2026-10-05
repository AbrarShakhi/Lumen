package com.abrarshakhi.lumen.feature.notes

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.core.ui.mvi.CollectEffects
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NotesRoute(
    onBack: () -> Unit,
    onOpenNote: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotesViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NotesScreen(
        state = state,
        onBack = onBack,
        onOpenNote = onOpenNote,
        modifier = modifier,
    )
}

@Composable
fun NoteEditorRoute(
    noteId: Long?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NoteEditorViewModel = koinViewModel { parametersOf(noteId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Loading and saving are the ViewModel's business: saving from a composable's onDispose
    // does not work, because the composition scope is already cancelled by then.
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            NoteEditorEffect.Close -> onClose()
        }
    }

    NoteEditorScreen(state = state, onIntent = viewModel::dispatch, modifier = modifier)
}
