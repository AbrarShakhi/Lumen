package com.abrarshakhi.lumen.feature.notes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.abrarshakhi.lumen.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorState,
    onIntent: (NoteEditorIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                // Deliberately untitled: the note's own title field is directly below, and
                // a second heading above it would just repeat what the user is typing.
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onIntent(NoteEditorIntent.Closed) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onIntent(NoteEditorIntent.Deleted) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.notes_delete),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            TextField(
                value = state.title,
                onValueChange = { onIntent(NoteEditorIntent.TitleChanged(it)) },
                placeholder = { Text(stringResource(R.string.notes_title_hint)) },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                colors = transparentFieldColours(),
                modifier = Modifier.fillMaxWidth(),
            )

            TextField(
                value = state.body,
                onValueChange = { onIntent(NoteEditorIntent.BodyChanged(it)) },
                placeholder = { Text(stringResource(R.string.notes_body_hint)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = transparentFieldColours(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Removes the filled-field chrome so the editor reads as a page rather than a form. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun transparentFieldColours() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
)
