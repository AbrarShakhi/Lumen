package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.text.TextValue

data class ResultAction(
    val id: String,
    val label: TextValue,
    val icon: IconSource,
    val kind: ActionKind,
    val requiresConfirmation: Boolean = false,
    val invoke: suspend (ActionContext) -> ActionOutcome,
)

data class ResultActions(
    val primary: ResultAction,
    val secondary: List<ResultAction> = emptyList(),
) {
    fun byId(actionId: String): ResultAction? =
        primary.takeIf { it.id == actionId } ?: secondary.firstOrNull { it.id == actionId }

    val all: List<ResultAction> get() = listOf(primary) + secondary
}

enum class ActionKind { Open, Call, Message, Copy, Share, Toggle, Edit, Delete, Configure }

interface ActionContext {
    val query: SearchQuery

    suspend fun copyToClipboard(label: String, text: String)

    suspend fun launch(intent: PlatformIntent): Boolean
}

sealed interface ActionOutcome {

    data object Handled : ActionOutcome

    data class Launch(val intent: PlatformIntent) : ActionOutcome

    data class ReplaceQuery(val query: String) : ActionOutcome

    data class Message(val text: TextValue) : ActionOutcome

    data class RequestPermissions(val permissions: List<AppPermission>) : ActionOutcome

    data class Navigate(val destination: InternalDestination) : ActionOutcome

    data class Failed(val cause: Throwable) : ActionOutcome
}

sealed interface InternalDestination {
    data object Settings : InternalDestination
    data object Providers : InternalDestination
    data object Triggers : InternalDestination
    data object SearchEngines : InternalDestination
    data object AiSettings : InternalDestination
    data object Notes : InternalDestination
    data object FileFolders : InternalDestination
    data class NoteEditor(val noteId: Long? = null) : InternalDestination
}
