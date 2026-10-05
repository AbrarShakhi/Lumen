package com.abrarshakhi.lumen.provider.ai

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.ai.AiAnswer
import com.abrarshakhi.lumen.core.domain.ai.AiBackend
import com.abrarshakhi.lumen.core.domain.ai.AiFailure
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.ExpandedContent
import com.abrarshakhi.lumen.core.domain.search.IconSource
import com.abrarshakhi.lumen.core.domain.search.InternalDestination
import com.abrarshakhi.lumen.core.domain.search.LumenIcon
import com.abrarshakhi.lumen.core.domain.search.ProviderId
import com.abrarshakhi.lumen.core.domain.search.ProviderMetadata
import com.abrarshakhi.lumen.core.domain.search.ProviderResults
import com.abrarshakhi.lumen.core.domain.search.ResultAction
import com.abrarshakhi.lumen.core.domain.search.ResultActions
import com.abrarshakhi.lumen.core.domain.search.ResultCategory
import com.abrarshakhi.lumen.core.domain.search.ResultId
import com.abrarshakhi.lumen.core.domain.search.SearchProvider
import com.abrarshakhi.lumen.core.domain.search.SearchQuery
import com.abrarshakhi.lumen.core.domain.search.SearchResult
import com.abrarshakhi.lumen.core.domain.search.TrailingContent
import com.abrarshakhi.lumen.core.domain.secret.SecretStore
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Answers a question with an AI model, on request.
 *
 * Only runs behind an explicit `ai ` prefix. Sending every keystroke to a paid API the user
 * did not ask to call would be both expensive and a privacy problem — the prefix makes the
 * network call something the user opts into per query, the same way `ggl` routes to Google.
 *
 * Streaming in the [SearchProvider.search] sense: it emits a pending row immediately so the
 * user can see the request was accepted, then replaces it with the answer.
 */
class AiAnswerProvider(
    private val backend: AiBackend,
    private val secrets: SecretStore,
    private val messages: AiMessages,
) : SearchProvider {

    override val id = ProviderId("ai")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_ai),
        category = ResultCategory.Answer,
        order = 5,
        // Long enough for a model to think; the pending row is on screen throughout.
        timeout = 40.seconds,
        // No debounce needed: nothing is sent until the query is submitted with the prefix,
        // and the prefix check is free.
        debounce = Duration.ZERO,
        minQueryLength = MIN_PREFIXED_LENGTH,
        defaultEnabled = true,
    )

    override fun search(query: SearchQuery): Flow<ProviderResults> = flow {
        val prompt = query.terms.aiPrompt() ?: return@flow

        val apiKey = secrets.get(backend.id.secretId)
        if (apiKey.isNullOrBlank()) {
            emit(ProviderResults(id, listOf(missingKeyResult())))
            return@flow
        }

        // Acknowledge immediately; a request that takes seconds with no feedback reads as
        // nothing having happened.
        emit(ProviderResults(id, listOf(pendingResult(prompt)), isPartial = true))

        val answer = backend.answer(prompt, apiKey)
        emit(ProviderResults(id, listOf(answerResult(prompt, answer))))
    }

    /** Returns the question after the `ai ` prefix, or null when it is not an AI query. */
    private fun String.aiPrompt(): String? {
        val trimmed = trim()
        val prefix = PREFIXES.firstOrNull { trimmed.startsWith("$it ", ignoreCase = true) }
            ?: return null
        return trimmed.substring(prefix.length).trim().takeIf { it.isNotEmpty() }
    }

    private fun pendingResult(prompt: String) = base(
        resultId = "ai:pending",
        title = prompt,
        subtitle = backend.displayName,
        trailing = TrailingContent.Progress,
        expanded = null,
        action = null,
    )

    private fun missingKeyResult() = base(
        resultId = "ai:no-key",
        title = messages.needsKey,
        subtitle = backend.displayName,
        trailing = null,
        expanded = null,
        // The useful thing to offer is the screen where a key is entered.
        action = ResultAction(
            id = "configure",
            label = TextValue.Res(R.string.ai_add_key),
            icon = IconSource.Vector(LumenIcon.Settings),
            kind = ActionKind.Configure,
            invoke = { ActionOutcome.Navigate(InternalDestination.AiSettings) },
        ),
    )

    private fun answerResult(prompt: String, answer: AiAnswer): SearchResult = when (answer) {
        is AiAnswer.Text -> base(
            resultId = "ai:answer",
            title = answer.content.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty(),
            subtitle = backend.displayName,
            trailing = null,
            expanded = ExpandedContent.Body(answer.content),
            action = ResultAction(
                id = "copy",
                label = TextValue.Res(R.string.action_copy),
                icon = IconSource.Vector(LumenIcon.Copy),
                kind = ActionKind.Copy,
                invoke = { context ->
                    context.copyToClipboard("Answer", answer.content)
                    ActionOutcome.Message(TextValue.Res(R.string.copied))
                },
            ),
        )

        is AiAnswer.Failed -> base(
            resultId = "ai:error",
            title = messages.forFailure(answer.reason),
            subtitle = backend.displayName,
            trailing = null,
            expanded = null,
            // Offering "Add key" only helps when the key is the problem; for an
            // account-level denial it would send the user in the wrong direction.
            action = if (answer.reason == AiFailure.InvalidKey || answer.reason == AiFailure.MissingKey) {
                ResultAction(
                    id = "configure",
                    label = TextValue.Res(R.string.ai_add_key),
                    icon = IconSource.Vector(LumenIcon.Settings),
                    kind = ActionKind.Configure,
                    invoke = { ActionOutcome.Navigate(InternalDestination.AiSettings) },
                )
            } else {
                null
            },
        )
    }

    private fun base(
        resultId: String,
        title: String,
        subtitle: String,
        trailing: TrailingContent?,
        expanded: ExpandedContent?,
        action: ResultAction?,
    ) = SearchResult(
        id = ResultId(resultId),
        providerId = id,
        title = title,
        subtitle = subtitle,
        icon = IconSource.Vector(LumenIcon.Ai),
        category = ResultCategory.Answer,
        score = 1f,
        rankingKey = "ai",
        trailing = trailing,
        expanded = expanded,
        triggerable = false,
        actions = ResultActions(
            primary = action ?: ResultAction(
                id = "noop",
                label = TextValue.Res(R.string.action_copy),
                icon = IconSource.Vector(LumenIcon.Ai),
                kind = ActionKind.Open,
                invoke = { ActionOutcome.Handled },
            ),
        ),
    )


    private companion object {
        val PREFIXES = listOf("ai", "ask")
        const val MIN_PREFIXED_LENGTH = 4
    }
}
