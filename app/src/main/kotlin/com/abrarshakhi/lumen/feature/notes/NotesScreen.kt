package com.abrarshakhi.lumen.feature.notes

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.repository.Note
import com.abrarshakhi.lumen.core.ui.component.EmptyState
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.contentColor
import com.abrarshakhi.lumen.core.ui.component.containerColor

@Composable
fun NotesScreen(
    state: NotesState,
    onBack: () -> Unit,
    onOpenNote: (Long?) -> Unit,
    onDeleteNote: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gridState = rememberLazyStaggeredGridState()
    val expandedFab by remember { derivedStateOf { gridState.firstVisibleItemIndex == 0 } }
    var pendingDelete by remember { mutableStateOf<Note?>(null) }

    LumenScaffold(
        title = stringResource(R.string.notes_title),
        subtitle = if (state.isEmpty) {
            null
        } else {
            pluralStringResource(R.plurals.notes_count, state.notes.size, state.notes.size)
        },
        onBack = onBack,
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.notes_new)) },
                icon = { Icon(Icons.Filled.EditNote, contentDescription = null) },
                onClick = { onOpenNote(null) },
                expanded = expandedFab,
            )
        },
    ) { innerPadding ->
        if (state.isEmpty) {
            EmptyState(
                title = stringResource(R.string.notes_empty),
                subtitle = stringResource(R.string.notes_empty_hint),
                modifier = Modifier.padding(innerPadding),
            )
            return@LumenScaffold
        }

        LazyVerticalStaggeredGrid(
            columns = StaggeredGridCells.Adaptive(NOTE_MIN_WIDTH),
            state = gridState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalItemSpacing = 10.dp,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            itemsIndexed(state.notes, key = { _, note -> note.id }) { index, note ->
                NoteCard(
                    note = note,
                    tone = NOTE_TONES[index % NOTE_TONES.size],
                    onClick = { onOpenNote(note.id) },
                    onLongClick = { pendingDelete = note },
                    modifier = Modifier.animateItem(),
                )
            }
        }
    }

    pendingDelete?.let { note ->
        DeleteNoteDialog(
            onConfirm = {
                onDeleteNote(note.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(
    note: Note,
    tone: IconTone,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content: Color = tone.contentColor()
    val body = remember(note.title, note.body) { note.cardBody() }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(tone.containerColor())
            .combinedClickable(
                role = Role.Button,
                onLongClickLabel = stringResource(R.string.notes_delete),
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = note.displayTitle.ifBlank { stringResource(R.string.notes_untitled) },
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = content,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (body.isNotBlank()) {
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = content,
                maxLines = BODY_MAX_LINES,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = relativeTime(note.updatedAtMillis),
            style = MaterialTheme.typography.labelSmall,
            color = content.copy(alpha = MUTED_ALPHA),
        )
    }
}

@Composable
internal fun DeleteNoteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
        title = { Text(stringResource(R.string.notes_delete_title)) },
        text = { Text(stringResource(R.string.notes_delete_body)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Text(stringResource(R.string.action_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text(stringResource(R.string.action_cancel))
            }
        },
    )
}

@Composable
private fun relativeTime(millis: Long): String {
    val now = System.currentTimeMillis()
    return if (now - millis < DateUtils.MINUTE_IN_MILLIS) {
        stringResource(R.string.notes_just_now)
    } else {
        DateUtils.getRelativeTimeSpanString(millis, now, DateUtils.MINUTE_IN_MILLIS).toString()
    }
}

private fun Note.cardBody(): String =
    if (title.isNotBlank()) {
        preview
    } else {
        body.lineSequence()
            .dropWhile { it.isBlank() }
            .drop(1)
            .joinToString(" ")
            .trim()
    }

private val NOTE_TONES = listOf(IconTone.Primary, IconTone.Secondary, IconTone.Tertiary, IconTone.Neutral)
private val NOTE_MIN_WIDTH = 150.dp
private const val BODY_MAX_LINES = 8
private const val MUTED_ALPHA = 0.72f
