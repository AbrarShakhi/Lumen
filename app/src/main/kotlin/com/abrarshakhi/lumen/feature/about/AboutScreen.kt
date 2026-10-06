package com.abrarshakhi.lumen.feature.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.abrarshakhi.lumen.BuildConfig
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.document.ProjectDocument
import com.abrarshakhi.lumen.core.domain.platform.IntentLauncher
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.ui.component.Entrance
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenBrandMark
import com.abrarshakhi.lumen.core.ui.component.LumenScaffold
import com.abrarshakhi.lumen.core.ui.component.LumenShape
import com.abrarshakhi.lumen.core.ui.component.SettingsGroup
import com.abrarshakhi.lumen.core.ui.component.SettingsItem
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun AboutRoute(
    onBack: () -> Unit,
    onOpenDocument: (ProjectDocument) -> Unit,
    modifier: Modifier = Modifier,
) {
    val launcher = koinInject<IntentLauncher>()
    val scope = rememberCoroutineScope()

    AboutScreen(
        onBack = onBack,
        onOpenDocument = onOpenDocument,
        onOpenUrl = { url -> scope.launch { launcher.launch(PlatformIntent.ViewUri(url)) } },
        modifier = modifier,
    )
}

@Composable
fun AboutScreen(
    onBack: () -> Unit,
    onOpenDocument: (ProjectDocument) -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LumenScaffold(
        title = stringResource(R.string.about_title),
        onBack = onBack,
        modifier = modifier,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item(key = "hero") { Entrance(index = 0) { AboutHero() } }

            item(key = "project") {
                Entrance(index = 1) {
                    SettingsGroup(
                        title = stringResource(R.string.about_group_project),
                        items = projectItems(onOpenUrl),
                    )
                }
            }

            item(key = "legal") {
                Entrance(index = 2) {
                    SettingsGroup(
                        title = stringResource(R.string.about_group_legal),
                        items = documentItems(onOpenDocument),
                    )
                }
            }

            item(key = "footer") {
                Text(
                    text = stringResource(R.string.about_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 24.dp),
                )
            }
        }
    }
}

@Composable
private fun AboutHero() {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LumenBrandMark(size = 132.dp)
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMediumEmphasized,
            )
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(R.string.about_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun projectItems(onOpenUrl: (String) -> Unit): List<SettingsItem> = listOf(
    SettingsItem.Link(
        key = "source",
        title = stringResource(R.string.about_source_code),
        summary = stringResource(R.string.about_source_code_summary),
        icon = Icons.Filled.Code,
        shape = LumenShape.Gem,
        tone = IconTone.Primary,
        onClick = { onOpenUrl(ProjectLinks.repository) },
    ),
    SettingsItem.Link(
        key = "star",
        title = stringResource(R.string.about_star),
        summary = stringResource(R.string.about_star_summary),
        icon = Icons.Filled.Star,
        shape = LumenShape.Sunny,
        tone = IconTone.Tertiary,
        onClick = { onOpenUrl(ProjectLinks.star) },
    ),
    SettingsItem.Link(
        key = "issue",
        title = stringResource(R.string.about_report_issue),
        summary = stringResource(R.string.about_report_issue_summary),
        icon = Icons.Filled.BugReport,
        shape = LumenShape.Cookie,
        tone = IconTone.Error,
        onClick = { onOpenUrl(ProjectLinks.newIssue) },
    ),
)

@Composable
private fun documentItems(onOpenDocument: (ProjectDocument) -> Unit): List<SettingsItem> =
    ProjectDocument.entries.map { document ->
        SettingsItem.Link(
            key = document.name,
            title = stringResource(document.titleRes()),
            icon = document.icon(),
            shape = LumenShape.Circle,
            tone = IconTone.Secondary,
            onClick = { onOpenDocument(document) },
        )
    }

private fun ProjectDocument.icon() = when (this) {
    ProjectDocument.About -> Icons.Filled.Info
    ProjectDocument.Credits -> Icons.Filled.Favorite
    ProjectDocument.Terms -> Icons.AutoMirrored.Filled.Article
    ProjectDocument.Privacy -> Icons.Filled.PrivacyTip
    ProjectDocument.License -> Icons.Filled.Gavel
}
