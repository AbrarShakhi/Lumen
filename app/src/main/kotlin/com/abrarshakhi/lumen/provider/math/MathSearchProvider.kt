package com.abrarshakhi.lumen.provider.math

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
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
import com.abrarshakhi.lumen.core.domain.util.NumberFormatting
import com.abrarshakhi.lumen.provider.math.calc.Calculator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Answers arithmetic typed into the search field.
 *
 * Ranked as an [ResultCategory.Answer] so a calculation appears above app matches — if you
 * typed a sum, the sum is what you wanted.
 */
class MathSearchProvider : SnapshotSearchProvider() {

    override val id = ProviderId("math")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_math),
        category = ResultCategory.Answer,
        order = 1,
        timeout = 100.milliseconds,
        debounce = Duration.ZERO,
        // "1+1" is the shortest real expression.
        minQueryLength = 3,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val result = Calculator.evaluate(query.terms) ?: return emptyList()
        val formatted = NumberFormatting.format(result.value)

        return listOf(
            SearchResult(
                id = ResultId("math:${query.terms}"),
                providerId = id,
                title = formatted,
                subtitle = query.terms.trim(),
                icon = IconSource.Vector(LumenIcon.Calculator),
                category = ResultCategory.Answer,
                score = 1f,
                // Every calculation is unique, so usage is tracked for the calculator as a
                // whole rather than per expression.
                rankingKey = "math",
                triggerable = false,
                actions = ResultActions(
                    primary = ResultAction(
                        id = "copy",
                        label = TextValue.Res(R.string.action_copy),
                        icon = IconSource.Vector(LumenIcon.Copy),
                        kind = ActionKind.Copy,
                        invoke = { context ->
                            // Copy the plain value, not the grouped display form — pasting
                            // "1,234" into another field rarely does what is wanted.
                            context.copyToClipboard(
                                "Result",
                                NumberFormatting.format(result.value, groupDigits = false),
                            )
                            ActionOutcome.Message(TextValue.Res(R.string.copied))
                        },
                    ),
                ),
            ),
        )
    }
}
