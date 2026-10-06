package com.abrarshakhi.lumen.feature.settings.files

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.ui.component.EmptyState
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenListColors
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.ShapedIcon
import org.koin.androidx.compose.koinViewModel

@Composable
fun FileFoldersRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FileFoldersViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree(),
    ) { uri -> uri?.let(viewModel::onFolderPicked) }

    FileFoldersScreen(
        state = state,
        onAddFolder = { picker.launch(null) },
        onRemoveFolder = viewModel::removeFolder,
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FileFoldersScreen(
    state: FileFoldersState,
    onAddFolder: () -> Unit,
    onRemoveFolder: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val expandedFab by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    LumenScaffold(
        title = stringResource(R.string.files_folders_title),
        subtitle = if (state.folders.isNotEmpty()) {
            pluralStringResource(R.plurals.files_indexed_count, state.indexedCount, state.indexedCount)
        } else {
            null
        },
        onBack = onBack,
        modifier = modifier,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.files_add_folder)) },
                icon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null) },
                onClick = onAddFolder,
                expanded = expandedFab,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
        ) {
            item(key = "explainer") {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShapedIcon(Icons.Filled.Info, shape = LumenShape.Cookie, tone = IconTone.Secondary)
                        Text(
                            text = stringResource(R.string.files_folders_explainer),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 14.dp),
                        )
                    }
                }
            }

            item(key = "progress") {
                AnimatedVisibility(visible = state.isIndexing) {
                    LinearWavyProgressIndicator(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    )
                }
            }

            if (state.folders.isEmpty()) {
                item(key = "empty") {
                    EmptyState(
                        title = stringResource(R.string.files_empty_title),
                        subtitle = stringResource(R.string.files_empty_body),
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }

            itemsIndexed(state.folders, key = { _, folder -> folder.uri }) { index, folder ->
                SegmentedListItem(
                    shapes = ListItemDefaults.segmentedShapes(index, state.folders.size),
                    modifier = Modifier.animateItem(),
                    colors = LumenListColors.raised(),
                    leadingContent = {
                        ShapedIcon(Icons.Filled.Folder, shape = LumenShape.Puffy, tone = IconTone.Tertiary)
                    },
                    trailingContent = {
                        IconButton(
                            onClick = { onRemoveFolder(folder.uri) },
                            shapes = IconButtonDefaults.shapes(),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.files_remove_folder),
                            )
                        }
                    },
                ) {
                    Text(folder.displayName)
                }
            }
        }
    }
}
