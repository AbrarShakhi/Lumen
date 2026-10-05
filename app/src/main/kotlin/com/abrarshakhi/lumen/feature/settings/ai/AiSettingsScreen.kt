package com.abrarshakhi.lumen.feature.settings.ai

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R

/**
 * Where an AI API key is entered.
 *
 * The stored key is never read back into this screen — only whether one exists. Displaying
 * a secret that is already saved adds no value and creates a way for it to be shoulder-read
 * or captured in a screenshot.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    state: AiSettingsState,
    onSave: (String) -> Unit,
    onRemove: () -> Unit,
    onOpenKeyUrl: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back),
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
                .padding(horizontal = 20.dp),
        ) {
            Text(
                text = state.backendName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                text = stringResource(
                    if (state.hasKey) R.string.ai_key_configured else R.string.ai_key_none,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(stringResource(R.string.ai_key_label)) },
                placeholder = { Text(stringResource(R.string.ai_key_hint)) },
                singleLine = true,
                // Masked: a key is a credential, not content.
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            )

            Button(
                onClick = {
                    onSave(draft)
                    draft = ""
                },
                enabled = draft.isNotBlank(),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Text(stringResource(R.string.action_save))
            }

            if (state.hasKey) {
                TextButton(onClick = onRemove) {
                    Text(stringResource(R.string.ai_key_remove))
                }
            }

            TextButton(onClick = onOpenKeyUrl, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.ai_get_key))
            }

            Text(
                text = stringResource(R.string.ai_usage_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 24.dp),
            )
        }
    }
}

data class AiSettingsState(
    val backendName: String = "",
    val hasKey: Boolean = false,
)
