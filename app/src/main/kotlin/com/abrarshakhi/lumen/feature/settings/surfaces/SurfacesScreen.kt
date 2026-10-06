package com.abrarshakhi.lumen.feature.settings.surfaces

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.platform.HomeWidget
import com.abrarshakhi.lumen.core.ui.component.Entrance
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.ShapedIcon

@Composable
fun SurfacesScreen(
    state: SurfacesState,
    onIntent: (SurfacesIntent) -> Unit,
    onBack: () -> Unit,
    onOpenLauncherMode: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    LumenScaffold(
        title = stringResource(R.string.surfaces_title),
        subtitle = stringResource(R.string.surfaces_subtitle),
        onBack = onBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "search-widget") {
                Entrance(index = 0) {
                    SurfaceCard(
                        title = stringResource(R.string.widget_search_label),
                        description = stringResource(R.string.surfaces_search_widget_body),
                        icon = Icons.Filled.Widgets,
                        shape = LumenShape.Clover,
                        tone = IconTone.Primary,
                        preview = { SearchWidgetPreview() },
                        action = pinAction(state, HomeWidget.Search, onIntent),
                    )
                }
            }

            item(key = "notes-widget") {
                Entrance(index = 1) {
                    SurfaceCard(
                        title = stringResource(R.string.widget_notes_label),
                        description = stringResource(R.string.surfaces_notes_widget_body),
                        icon = Icons.AutoMirrored.Filled.StickyNote2,
                        shape = LumenShape.Cookie,
                        tone = IconTone.Tertiary,
                        preview = { NotesWidgetPreview() },
                        action = pinAction(state, HomeWidget.Notes, onIntent),
                    )
                }
            }

            item(key = "tile") {
                Entrance(index = 2) {
                    SurfaceCard(
                        title = stringResource(R.string.surfaces_tile_title),
                        description = stringResource(
                            if (state.canRequestTile) {
                                R.string.surfaces_tile_body
                            } else {
                                R.string.surfaces_tile_manual
                            },
                        ),
                        icon = Icons.Filled.TouchApp,
                        shape = LumenShape.Sunny,
                        tone = IconTone.Secondary,
                        preview = { TilePreview() },
                        action = if (state.canRequestTile) {
                            CardAction(stringResource(R.string.surfaces_add_tile)) {
                                onIntent(SurfacesIntent.AddTile)
                            }
                        } else {
                            null
                        },
                    )
                }
            }

            item(key = "assistant") {
                Entrance(index = 3) {
                    SurfaceCard(
                        title = stringResource(R.string.surfaces_assistant_title),
                        description = stringResource(R.string.surfaces_assistant_body),
                        icon = Icons.Filled.KeyboardVoice,
                        shape = LumenShape.Gem,
                        tone = IconTone.Primary,
                        action = CardAction(stringResource(R.string.surfaces_assistant_action)) {
                            onIntent(SurfacesIntent.OpenAssistantSettings)
                        },
                    )
                }
            }

            item(key = "launcher") {
                Entrance(index = 4) {
                    SurfaceCard(
                        title = stringResource(R.string.launcher_title),
                        description = stringResource(R.string.launcher_explainer),
                        icon = Icons.Filled.Home,
                        shape = LumenShape.Arch,
                        tone = IconTone.Secondary,
                        action = CardAction(stringResource(R.string.surfaces_launcher_action), tonal = true) {
                            onOpenLauncherMode()
                        },
                    )
                }
            }
        }
    }
}

private class CardAction(
    val label: String,
    val tonal: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
private fun pinAction(
    state: SurfacesState,
    widget: HomeWidget,
    onIntent: (SurfacesIntent) -> Unit,
): CardAction? = if (state.canPinWidgets) {
    CardAction(stringResource(R.string.surfaces_add_widget)) {
        onIntent(SurfacesIntent.PinWidget(widget))
    }
} else {
    null
}

@Composable
private fun SurfaceCard(
    title: String,
    description: String,
    icon: ImageVector,
    shape: LumenShape,
    tone: IconTone,
    modifier: Modifier = Modifier,
    preview: (@Composable () -> Unit)? = null,
    action: CardAction? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceBright,
    ) {
        Column(Modifier.padding(20.dp)) {
            preview?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    it()
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ShapedIcon(icon = icon, shape = shape, tone = tone, size = 44.dp)
                Column(Modifier.padding(start = 14.dp).weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            action?.let {
                Spacer(Modifier.height(16.dp))
                if (it.tonal) {
                    FilledTonalButton(
                        onClick = it.onClick,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.align(Alignment.End),
                    ) { Text(it.label) }
                } else {
                    Button(
                        onClick = it.onClick,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(it.label)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchWidgetPreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 10.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher_round),
            contentDescription = null,
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = stringResource(R.string.widget_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(start = 12.dp).weight(1f),
        )
        Icon(
            imageVector = Icons.Filled.EditNote,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun NotesWidgetPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.StickyNote2,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.notes_title),
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp).weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
        listOf(R.string.widget_notes_preview_first, R.string.widget_notes_preview_second).forEach { text ->
            Text(
                text = stringResource(text),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun TilePreview() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = stringResource(R.string.tile_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(start = 10.dp),
        )
    }
}
