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

/**
 * The composition root.
 *
 * Owns exactly three things: the theme, the back stack, and the app-level snackbar. It
 * deliberately does *not* own a Scaffold shared across destinations — the search surface is
 * full-bleed with no top bar, while settings screens are ordinary lists, so a shared
 * chrome layer would be an abstraction serving neither. Each destination brings its own.
 *
 * App-wide preferences are read here from a singleton rather than a "main" ViewModel:
 * theme and font scale outlive every screen, and routing them through a screen-scoped
 * ViewModel is exactly how a god object starts.
 */
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
                // Back at the root closes Lumen rather than trapping the user on a
                // surface they summoned — it behaves like a dismissible overlay.
                onBack = { if (!backStack.popOrFalse()) onFinish() },
                entryDecorators = listOf(
                    // Scopes rememberSaveable state (scroll position, expanded rows) per
                    // entry, so returning to a destination restores it rather than
                    // inheriting the previous screen's.
                    rememberSaveableStateHolderNavEntryDecorator(),
                    // Gives each entry its own ViewModelStore, cleared when it is popped.
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
