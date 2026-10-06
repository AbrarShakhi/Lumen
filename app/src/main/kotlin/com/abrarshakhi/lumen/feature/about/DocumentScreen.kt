package com.abrarshakhi.lumen.feature.about

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.ui.component.EmptyState
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.MarkdownBlockView
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun DocumentRoute(
    document: ProjectDocument,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentViewModel = koinViewModel(key = document.name) { parametersOf(document) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val launcher = koinInject<IntentLauncher>()
    val scope = rememberCoroutineScope()

    DocumentScreen(
        state = state,
        onRetry = { viewModel.dispatch(DocumentIntent.Load) },
        onOpenUrl = { url -> scope.launch { launcher.launch(PlatformIntent.ViewUri(url)) } },
        onBack = onBack,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DocumentScreen(
    state: DocumentState,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LumenScaffold(
        title = stringResource(state.document.titleRes()),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        AnimatedContent(
            targetState = state.content,
            contentKey = { it::class },
            label = "document-content",
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) { content ->
            when (content) {
                DocumentContent.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingIndicator(Modifier.size(48.dp))
                }

                DocumentContent.Unavailable -> EmptyState(
                    title = stringResource(R.string.document_unavailable_title),
                    subtitle = stringResource(R.string.document_unavailable_body),
                    action = {
                        Button(onClick = onRetry, shapes = ButtonDefaults.shapes()) {
                            Text(stringResource(R.string.action_retry))
                        }
                    },
                )

                is DocumentContent.Ready -> Surface(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    shape = MaterialTheme.shapes.extraLarge.copy(
                        bottomStart = MaterialTheme.shapes.extraSmall.bottomStart,
                        bottomEnd = MaterialTheme.shapes.extraSmall.bottomEnd,
                    ),
                    color = MaterialTheme.colorScheme.surfaceBright,
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        itemsIndexed(content.blocks) { _, block ->
                            MarkdownBlockView(block = block, onOpenUrl = onOpenUrl)
                        }
                    }
                }
            }
        }
    }
}

internal fun ProjectDocument.titleRes(): Int = when (this) {
    ProjectDocument.About -> R.string.document_about
    ProjectDocument.Credits -> R.string.document_credits
    ProjectDocument.Terms -> R.string.document_terms
    ProjectDocument.Privacy -> R.string.document_privacy
    ProjectDocument.License -> R.string.document_license
}
