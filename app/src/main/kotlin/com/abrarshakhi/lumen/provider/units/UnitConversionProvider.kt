package com.abrarshakhi.lumen.provider.units

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
import com.abrarshakhi.lumen.core.domain.search.TrailingContent
import com.abrarshakhi.lumen.core.domain.text.TextValue
import com.abrarshakhi.lumen.core.domain.util.NumberFormatting
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Converts between units typed inline, e.g. `20cm in inches`. */
class UnitConversionProvider : SnapshotSearchProvider() {

    override val id = ProviderId("units")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_units),
        category = ResultCategory.Answer,
        order = 2,
        timeout = 100.milliseconds,
        debounce = Duration.ZERO,
        // "5 m in ft" is the shortest meaningful conversion.
        minQueryLength = 6,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val conversion = ConversionParser.parse(query.terms) ?: return emptyList()

        val formatted = NumberFormatting.format(conversion.result)
        val answer = "$formatted ${conversion.to.symbol}"
        val source = "${NumberFormatting.format(conversion.value)} ${conversion.from.symbol}"

        return listOf(
            SearchResult(
                id = ResultId("units:${conversion.from.id}:${conversion.to.id}:${conversion.value}"),
                providerId = id,
                title = answer,
                subtitle = "$source · ${conversion.from.dimension.displayName}",
                icon = IconSource.Vector(LumenIcon.Convert),
                category = ResultCategory.Answer,
                score = 1f,
                // Conversions are one-off; a per-query ranking key would learn nothing, so
                // usage accrues against the dimension pair instead.
                rankingKey = "units:${conversion.from.id}:${conversion.to.id}",
                trailing = TrailingContent.Text(conversion.to.symbol),
                triggerable = false,
                actions = ResultActions(
                    primary = ResultAction(
                        id = "copy",
                        label = TextValue.Res(R.string.action_copy),
                        icon = IconSource.Vector(LumenIcon.Copy),
                        kind = ActionKind.Copy,
                        invoke = { context ->
                            context.copyToClipboard("Conversion", formatted)
                            ActionOutcome.Message(TextValue.Res(R.string.copied))
                        },
                    ),
                ),
            ),
        )
    }
}
