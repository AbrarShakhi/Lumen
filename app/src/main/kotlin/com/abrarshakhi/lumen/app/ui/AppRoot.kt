package com.abrarshakhi.lumen.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.NavDisplay
import com.abrarshakhi.lumen.app.LaunchEvent
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.app.navigation.initialStack
import com.abrarshakhi.lumen.app.navigation.navigateTo
import com.abrarshakhi.lumen.app.navigation.popOrFalse
import com.abrarshakhi.lumen.app.navigation.replaceWith
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import com.abrarshakhi.lumen.core.ui.theme.LumenTheme
import com.abrarshakhi.lumen.feature.notes.NoteEditorRoute
import com.abrarshakhi.lumen.feature.notes.NotesRoute
import com.abrarshakhi.lumen.feature.search.SearchPresentation
import com.abrarshakhi.lumen.feature.search.SearchRoute
import com.abrarshakhi.lumen.feature.settings.SettingsRoute
import com.abrarshakhi.lumen.feature.settings.ai.AiSettingsRoute
import com.abrarshakhi.lumen.feature.settings.files.FileFoldersRoute
import com.abrarshakhi.lumen.feature.settings.launcher.LauncherSettingsRoute
import com.abrarshakhi.lumen.feature.settings.providers.ProvidersRoute
import com.abrarshakhi.lumen.feature.settings.surfaces.SurfacesRoute
import org.koin.compose.koinInject

@Composable
fun AppRoot(
    launch: LaunchEvent,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
    presentation: SearchPresentation = SearchPresentation.Fullscreen,
) {
    val preferencesRepository = koinInject<UserPreferencesRepository>()
    val preferences by preferencesRepository.preferences.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(*launch.request.route.initialStack().toTypedArray())
    var appliedSerial by rememberSaveable { mutableIntStateOf(launch.serial) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(launch.serial) {
        if (launch.serial != appliedSerial) {
            appliedSerial = launch.serial
            backStack.replaceWith(launch.request.route.initialStack())
        }
    }

    val navigate: (AppRouteKey) -> Unit = { route -> backStack.navigateTo(route) }
    val back: () -> Unit = { if (!backStack.popOrFalse()) onFinish() }

    LumenTheme(preferences = preferences) {
        val motion = MaterialTheme.motionScheme
        Box(modifier.fillMaxSize()) {
            NavDisplay(
                backStack = backStack,
                onBack = back,
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                transitionSpec = { forwardTransition(motion) },
                popTransitionSpec = { backwardTransition(motion) },
                predictivePopTransitionSpec = { backwardTransition(motion) },
                entryProvider = entryProvider(
                    fallback = { key -> NavEntry(key) { SettingsRoute(onBack = back, onNavigate = navigate) } },
                ) {
                    entry<AppRouteKey.Search> {
                        SearchRoute(
                            onNavigate = navigate,
                            onCloseSurface = onFinish,
                            snackbarHostState = snackbarHostState,
                            launchQuery = launch.request.prefilledQuery,
                            launchSerial = launch.serial,
                            autoFocus = launch.request.focusInput,
                            barPosition = preferences.searchBarPosition,
                            presentation = presentation,
                        )
                    }

                    entry<AppRouteKey.Settings> {
                        SettingsRoute(onBack = back, onNavigate = navigate)
                    }

                    entry<AppRouteKey.Providers> { ProvidersRoute(onBack = back) }

                    entry<AppRouteKey.AiSettings> { AiSettingsRoute(onBack = back) }

                    entry<AppRouteKey.LauncherMode> { LauncherSettingsRoute(onBack = back) }

                    entry<AppRouteKey.FileFolders> { FileFoldersRoute(onBack = back) }

                    entry<AppRouteKey.Surfaces> {
                        SurfacesRoute(onBack = back, onNavigate = navigate)
                    }

                    entry<AppRouteKey.Notes> {
                        NotesRoute(
                            onBack = back,
                            onOpenNote = { noteId -> navigate(AppRouteKey.NoteEditor(noteId)) },
                        )
                    }

                    entry<AppRouteKey.NoteEditor> { route ->
                        NoteEditorRoute(noteId = route.noteId, onClose = back)
                    }
                },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
            )
        }
    }
}

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.forwardTransition(
    motion: MotionScheme,
): ContentTransform =
    (slideInHorizontally(motion.defaultSpatialSpec()) { it / ENTER_DIVISOR } +
        fadeIn(motion.defaultEffectsSpec()))
        .togetherWith(
            slideOutHorizontally(motion.defaultSpatialSpec()) { -it / EXIT_DIVISOR } +
                fadeOut(motion.fastEffectsSpec()),
        )

private fun <T : Any> AnimatedContentTransitionScope<Scene<T>>.backwardTransition(
    motion: MotionScheme,
): ContentTransform =
    (slideInHorizontally(motion.defaultSpatialSpec()) { -it / EXIT_DIVISOR } +
        fadeIn(motion.defaultEffectsSpec()))
        .togetherWith(
            slideOutHorizontally(motion.defaultSpatialSpec()) { it / ENTER_DIVISOR } +
                fadeOut(motion.fastEffectsSpec()),
        )

private const val ENTER_DIVISOR = 4
private const val EXIT_DIVISOR = 8
