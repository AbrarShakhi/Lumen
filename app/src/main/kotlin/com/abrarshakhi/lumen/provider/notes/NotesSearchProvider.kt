package com.abrarshakhi.lumen.provider.notes

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.repository.Note
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.InternalDestination
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.SnapshotSearchProvider
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration.Companion.milliseconds

/**
 * Searches the user's notes.
 *
 * Backed by SQLite full-text search rather than the in-memory fuzzy matcher the other
 * providers use: note bodies are unbounded text, and scanning every one of them on each
 * keystroke would not stay fast. The trade is that matching is word-prefix based rather
 * than typo-tolerant.
 */
class NotesSearchProvider(
    private val notes: NoteRepository,
) : SnapshotSearchProvider() {

    override val id = ProviderId("notes")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_notes),
        category = ResultCategory.Note,
        order = 50,
        timeout = 400.milliseconds,
        // Touches the database, so it is worth not firing on every keystroke.
        debounce = 80.milliseconds,
        minQueryLength = 2,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> =
        notes.search(query.terms, limit = MAX_RESULTS).map { it.toResult() }

    private fun Note.toResult() = SearchResult(
        id = ResultId("note:$id"),
        providerId = this@NotesSearchProvider.id,
        title = displayTitle,
        subtitle = preview.take(PREVIEW_LENGTH).ifBlank { null },
        icon = IconSource.Vector(LumenIcon.Note),
        category = ResultCategory.Note,
        // FTS reports no usable relevance score here, so ordering comes from the query's
        // recency sort and results share a single relevance value.
        score = 0.8f,
        rankingKey = "note:$id",
        actions = ResultActions(
            primary = ResultAction(
                id = "open-note",
                label = TextValue.Res(R.string.action_edit),
                icon = IconSource.Vector(LumenIcon.Edit),
                kind = ActionKind.Edit,
                invoke = { ActionOutcome.Navigate(InternalDestination.NoteEditor(id)) },
            ),
            secondary = listOf(
                ResultAction(
                    id = "copy-note",
                    label = TextValue.Res(R.string.action_copy),
                    icon = IconSource.Vector(LumenIcon.Copy),
                    kind = ActionKind.Copy,
                    invoke = { context ->
                        context.copyToClipboard(displayTitle, body)
                        ActionOutcome.Message(TextValue.Res(R.string.copied))
                    },
                ),
            ),
        ),
    )

    private companion object {
        const val MAX_RESULTS = 10
        const val PREVIEW_LENGTH = 90
    }
}
