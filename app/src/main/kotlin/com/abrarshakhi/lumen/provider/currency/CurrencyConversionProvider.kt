package com.abrarshakhi.lumen.provider.currency

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.search.ActionKind
import com.abrarshakhi.lumen.core.domain.search.ActionOutcome
import com.abrarshakhi.lumen.core.domain.search.IconSource
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
import com.abrarshakhi.lumen.core.domain.text.TextValue
import com.abrarshakhi.lumen.core.domain.util.NumberFormatting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class CurrencyConversionProvider(
    private val rates: CurrencyRatesRepository,
) : SearchProvider {

    override val id = ProviderId("currency")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_currency),
        category = ResultCategory.Answer,
        icon = LumenIcon.Currency,
        order = 4,
        timeout = 8.seconds,
        debounce = 250.milliseconds,
        minQueryLength = 8,
    )

    override fun search(query: SearchQuery): Flow<ProviderResults> = flow {
        val request = CurrencyQueryParser.parse(query.terms, ExchangeRatesClient.SUPPORTED)
            ?: return@flow

        val cached = rates.cachedRates(request.from)?.let { result(request, it) }
        if (cached != null) {
            emit(ProviderResults(providerId = id, results = listOf(cached), isPartial = true))
        }

        val fresh = rates.ratesFor(request.from)?.let { result(request, it) }
        when {
            fresh != null -> emit(ProviderResults(providerId = id, results = listOf(fresh)))
            cached == null -> emit(ProviderResults.empty(id))
            else -> emit(ProviderResults(providerId = id, results = listOf(cached)))
        }
    }

    private fun result(
        request: CurrencyQueryParser.Request,
        rates: CurrencyRatesRepository.Rates,
    ): SearchResult? {
        val rate = rates.table.rates[request.to] ?: return null
        val converted = request.amount * rate
        val formatted = NumberFormatting.format(converted)

        return SearchResult(
            id = ResultId("currency:${request.from}:${request.to}:${request.amount}"),
            providerId = id,
            title = "$formatted ${request.to}",
            subtitle = buildString {
                append(NumberFormatting.format(request.amount))
                append(' ')
                append(request.from)
                append(" · 1 ${request.from} = ${NumberFormatting.format(rate)} ${request.to}")
                if (rates.isStale) append(" · offline rate")
            },
            icon = IconSource.Vector(LumenIcon.Currency),
            category = ResultCategory.Answer,
            score = 1f,
            rankingKey = "currency",
            trailing = TrailingContent.Text(request.to),
            triggerable = false,
            actions = ResultActions(
                primary = ResultAction(
                    id = "copy",
                    label = TextValue.Res(R.string.action_copy),
                    icon = IconSource.Vector(LumenIcon.Copy),
                    kind = ActionKind.Copy,
                    invoke = { context ->
                        context.copyToClipboard(
                            "Conversion",
                            NumberFormatting.format(converted, groupDigits = false),
                        )
                        ActionOutcome.Message(TextValue.Res(R.string.copied))
                    },
                ),
            ),
        )
    }
}
