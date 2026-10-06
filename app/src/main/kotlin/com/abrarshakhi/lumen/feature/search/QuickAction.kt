package com.abrarshakhi.lumen.feature.search

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.ui.graphics.vector.ImageVector
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.core.ui.component.IconTone
import com.abrarshakhi.lumen.core.ui.component.LumenShape

sealed interface QuickAction {
    data class Navigate(val route: AppRouteKey) : QuickAction
    data class Prefill(val query: String) : QuickAction
}

enum class QuickTile(
    @param:StringRes val label: Int,
    val icon: ImageVector,
    val shape: LumenShape,
    val tone: IconTone,
    val action: QuickAction,
) {
    Notes(
        label = R.string.quick_notes,
        icon = Icons.AutoMirrored.Filled.StickyNote2,
        shape = LumenShape.Cookie,
        tone = IconTone.Primary,
        action = QuickAction.Navigate(AppRouteKey.Notes),
    ),
    NewNote(
        label = R.string.quick_new_note,
        icon = Icons.Filled.EditNote,
        shape = LumenShape.Clover,
        tone = IconTone.Tertiary,
        action = QuickAction.Navigate(AppRouteKey.NoteEditor()),
    ),
    AskAi(
        label = R.string.quick_ask_ai,
        icon = Icons.Filled.AutoAwesome,
        shape = LumenShape.Sunny,
        tone = IconTone.Secondary,
        action = QuickAction.Prefill(AI_PREFIX),
    ),
    Settings(
        label = R.string.quick_settings,
        icon = Icons.Filled.Settings,
        shape = LumenShape.Gem,
        tone = IconTone.Neutral,
        action = QuickAction.Navigate(AppRouteKey.Settings),
    ),
}

enum class QuerySuggestion(val query: String, val icon: ImageVector) {
    Math("(12 + 8) * 3", Icons.Filled.Calculate),
    Units("20 cm in inches", Icons.Filled.SwapHoriz),
    Currency("100 usd in eur", Icons.Filled.CurrencyExchange),
    WorldClock("time in tokyo", Icons.Filled.Schedule),
}

private const val AI_PREFIX = "ai "
