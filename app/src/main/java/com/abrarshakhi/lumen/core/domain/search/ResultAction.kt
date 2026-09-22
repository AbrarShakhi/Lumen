package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.text.TextValue

/**
 * Something the user can do with a [SearchResult].
 *
 * [invoke] is a lambda rather than a data-described command deliberately. The alternative —
 * a sealed `ResultCommand` hierarchy interpreted centrally — would require a `when` that
 * grows a branch for every new provider, which is exactly the open/closed violation this
 * architecture exists to avoid. Keeping behaviour next to the provider that produced it
 * means adding a provider touches only that provider's own package.
 *
 * The cost is that [SearchResult] equality is meaningless, so results are compared and
 * keyed by [ResultId], never by structural equality.
 */
data class ResultAction(
    val id: String,
    val label: TextValue,
    val icon: IconSource,
    val kind: ActionKind,
    /** When true the UI confirms before invoking — for destructive actions. */
    val requiresConfirmation: Boolean = false,
    val invoke: suspend (ActionContext) -> ActionOutcome,
)

/**
 * The primary action plus any alternatives.
 *
 * [primary] fires on tap and on the IME action key when the result is ranked first.
 * [secondary] renders as trailing icon buttons on the row and in full in the long-press sheet.
 */
data class ResultActions(
    val primary: ResultAction,
    val secondary: List<ResultAction> = emptyList(),
) {
    fun byId(actionId: String): ResultAction? =
        primary.takeIf { it.id == actionId } ?: secondary.firstOrNull { it.id == actionId }

    val all: List<ResultAction> get() = listOf(primary) + secondary
}

/** Semantic category of an action, used to pick default iconography and confirmation copy. */
enum class ActionKind { Open, Call, Message, Copy, Share, Toggle, Edit, Delete, Configure }

/**
 * Capabilities handed to an action when it runs, so providers never need a `Context`.
 */
interface ActionContext {
    val query: SearchQuery

    suspend fun copyToClipboard(label: String, text: String)

    /** Launches [intent]; returns false when no activity resolved. */
    suspend fun launch(intent: PlatformIntent): Boolean
}

/**
 * What invoking an action produced.
 *
 * A closed set over outcomes rather than over providers, so the single `when` that
 * interprets it never grows as providers are added. [Launch] carries data rather than
 * performing the launch, which is what lets non-UI surfaces reuse the same action.
 */
sealed interface ActionOutcome {

    /** The provider did the work itself; nothing further is required. */
    data object Handled : ActionOutcome

    data class Launch(val intent: PlatformIntent) : ActionOutcome

    /** Replace the search field's contents, e.g. when picking a search-engine prefix. */
    data class ReplaceQuery(val query: String) : ActionOutcome

    data class Message(val text: TextValue) : ActionOutcome

    data class RequestPermissions(val permissions: List<AppPermission>) : ActionOutcome

    /** Navigate somewhere in Lumen. Mapped to a concrete route in the app layer. */
    data class Navigate(val destination: InternalDestination) : ActionOutcome

    data class Failed(val cause: Throwable) : ActionOutcome
}

/**
 * Internal destinations the domain layer may request.
 *
 * Deliberately not the app layer's route types — the domain must not depend on navigation —
 * but a sealed hierarchy rather than an enum, because some destinations carry an argument
 * (which note to open) and an enum cannot.
 */
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
