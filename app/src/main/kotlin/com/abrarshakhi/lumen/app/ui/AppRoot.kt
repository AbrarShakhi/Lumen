package com.abrarshakhi.lumen.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.app.navigation.navigateTo
import com.abrarshakhi.lumen.app.navigation.popOrFalse
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.ui.theme.LumenTheme
import com.abrarshakhi.lumen.feature.search.SearchPresentation
import com.abrarshakhi.lumen.feature.search.SearchRoute
import com.abrarshakhi.lumen.feature.notes.NoteEditorRoute
import com.abrarshakhi.lumen.feature.notes.NotesRoute
import com.abrarshakhi.lumen.feature.settings.SettingsRoute
import com.abrarshakhi.lumen.feature.settings.ai.AiSettingsRoute
import com.abrarshakhi.lumen.feature.settings.files.FileFoldersRoute
import com.abrarshakhi.lumen.feature.settings.launcher.LauncherSettingsRoute
import com.abrarshakhi.lumen.feature.settings.providers.ProvidersRoute
import org.koin.compose.koinInject

@Composable
fun AppRoot(
    startRoute: AppRouteKey,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    initialQuery: String? = null,
    autoFocus: Boolean = true,
    presentation: SearchPresentation = SearchPresentation.Fullscreen,
) {
    val preferencesRepository = koinInject<UserPreferencesRepository>()
    val preferences by preferencesRepository.preferences.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(startRoute)
    val snackbarHostState = remember { SnackbarHostState() }

    LumenTheme(preferences = preferences) {
        Box(modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = { if (!backStack.popOrFalse()) onFinish() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<AppRouteKey.Search> {
                        SearchRoute(
                            onNavigate = { route -> backStack.navigateTo(route as NavKey) },
                            onCloseSurface = onFinish,
                            snackbarHostState = snackbarHostState,
                            initialQuery = initialQuery,
                            autoFocus = autoFocus,
                            barPosition = preferences.searchBarPosition,
                            presentation = presentation,
                        )
                    }

                    entry<AppRouteKey.Settings> {
                        SettingsRoute(
                            onBack = { backStack.removeLastOrNull() },
                            onOpenProviders = { backStack.navigateTo(AppRouteKey.Providers) },
                            onOpenFileFolders = { backStack.navigateTo(AppRouteKey.FileFolders) },
                            onOpenLauncherMode = { backStack.navigateTo(AppRouteKey.LauncherMode) },
                        )
                    }

                    entry<AppRouteKey.Providers> {
                        ProvidersRoute(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<AppRouteKey.AiSettings> {
                        AiSettingsRoute(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<AppRouteKey.LauncherMode> {
                        LauncherSettingsRoute(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<AppRouteKey.FileFolders> {
                        FileFoldersRoute(onBack = { backStack.removeLastOrNull() })
                    }

                    entry<AppRouteKey.Notes> {
                        NotesRoute(
                            onBack = { backStack.removeLastOrNull() },
                            onOpenNote = { noteId ->
                                backStack.navigateTo(AppRouteKey.NoteEditor(noteId))
                            },
                        )
                    }

                    entry<AppRouteKey.NoteEditor> { route ->
                        NoteEditorRoute(
                            noteId = route.noteId,
                            onClose = { backStack.removeLastOrNull() },
                        )
                    }
                },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
