package com.abrarshakhi.lumen.feature.settings.ai

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.ShapedIcon

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
    val submit = {
        onSave(draft)
        draft = ""
    }

    LumenScaffold(
        title = stringResource(R.string.ai_title),
        subtitle = stringResource(R.string.ai_powered_by, state.providerLabel),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            KeyStatusCard(hasKey = state.hasKey, providerLabel = state.providerLabel)

            SectionCard(
                icon = Icons.Filled.Key,
                title = stringResource(R.string.ai_key_title, state.providerLabel),
                tone = IconTone.Secondary,
                shape = LumenShape.Gem,
            ) {
                SupportedProviderNotice(
                    providerLabel = state.providerLabel,
                    keySourceName = state.keySourceName,
                )
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = { Text(stringResource(R.string.ai_key_field_label, state.providerLabel)) },
                    placeholder = { Text(stringResource(R.string.ai_key_hint)) },
                    supportingText = { Text(stringResource(R.string.ai_key_field_support, state.keySourceName)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                ) {
                    if (state.hasKey) {
                        TextButton(onClick = onRemove, shapes = ButtonDefaults.shapes()) {
                            Text(stringResource(R.string.ai_key_remove))
                        }
                    }
                    Button(
                        onClick = submit,
                        enabled = draft.isNotBlank(),
                        shapes = ButtonDefaults.shapes(),
                    ) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }

            SectionCard(
                icon = Icons.Filled.Lightbulb,
                title = stringResource(R.string.ai_how_title),
                tone = IconTone.Tertiary,
                shape = LumenShape.Sunny,
            ) {
                Text(
                    text = stringResource(R.string.ai_usage_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(
                    onClick = onOpenKeyUrl,
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(stringResource(R.string.ai_get_key_from, state.keySourceName))
                    Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun SupportedProviderNotice(providerLabel: String, keySourceName: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Verified,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    text = stringResource(R.string.ai_supported_title, providerLabel),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                )
                Text(
                    text = stringResource(R.string.ai_supported_body, providerLabel, keySourceName),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun KeyStatusCard(hasKey: Boolean, providerLabel: String) {
    val container by animateColorAsState(
        targetValue = if (hasKey) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "ai-status",
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = container,
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShapedIcon(
                icon = Icons.Filled.AutoAwesome,
                shape = LumenShape.SoftBurst,
                tone = if (hasKey) IconTone.Primary else IconTone.Neutral,
                size = 56.dp,
            )
            Column(Modifier.padding(start = 16.dp)) {
                Text(
                    text = stringResource(if (hasKey) R.string.ai_ready_title else R.string.ai_key_none),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                )
                Text(
                    text = if (hasKey) {
                        stringResource(R.string.ai_key_configured_for, providerLabel)
                    } else {
                        stringResource(R.string.ai_needs_key_for, providerLabel)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    tone: IconTone,
    shape: LumenShape,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapedIcon(icon = icon, tone = tone, shape = shape)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    modifier = Modifier.padding(start = 14.dp),
                )
            }
            Column(Modifier.padding(top = 16.dp)) { content() }
        }
    }
}

data class AiSettingsState(
    val backendName: String = "",
    val vendorName: String = "",
    val keySourceName: String = "",
    val hasKey: Boolean = false,
) {
    val providerLabel: String get() = listOf(vendorName, backendName).filter { it.isNotBlank() }.joinToString(" ")
}
