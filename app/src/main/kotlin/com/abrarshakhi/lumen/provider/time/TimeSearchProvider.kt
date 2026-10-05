package com.abrarshakhi.lumen.provider.time

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
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.milliseconds
import kotlin.math.abs
import kotlin.math.roundToLong

/** World clock and simple date arithmetic. */
class TimeSearchProvider(
    private val now: () -> ZonedDateTime = { ZonedDateTime.now() },
) : SnapshotSearchProvider() {

    override val id = ProviderId("time")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_time),
        category = ResultCategory.Answer,
        order = 3,
        timeout = 100.milliseconds,
        debounce = kotlin.time.Duration.ZERO,
        minQueryLength = 6,
    )

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val current = now()
        return when (val parsed = TimeQueryParser.parse(query.terms, current.toLocalDate())) {
            null -> emptyList()
            is TimeQueryParser.Parsed.Clock -> listOf(clockResult(parsed, current))
            is TimeQueryParser.Parsed.DateOffset -> listOf(dateResult(parsed))
        }
    }

    private fun clockResult(
        parsed: TimeQueryParser.Parsed.Clock,
        current: ZonedDateTime,
    ): SearchResult {
        val there = current.withZoneSameInstant(parsed.place.zone)
        val time = there.format(TIME_FORMAT)
        val date = there.format(DATE_FORMAT)

        return answer(
            resultId = "time:clock:${parsed.place.zone.id}",
            title = time,
            subtitle = "${parsed.place.displayName} · $date${offsetSuffix(current, there)}",
            icon = LumenIcon.Clock,
            copyValue = "$time ${parsed.place.displayName}",
        )
    }

    /** "+4h from you" — the difference is usually the reason for asking. */
    private fun offsetSuffix(here: ZonedDateTime, there: ZonedDateTime): String {
        val minutes = Duration.between(
            here.toLocalDateTime(),
            there.toLocalDateTime(),
        ).toMinutes()
        if (minutes == 0L) return ""

        val hours = abs(minutes) / 60.0
        val sign = if (minutes > 0) "+" else "-"
        val rendered = if (hours % 1.0 == 0.0) "${hours.roundToLong()}h" else "${hours}h"
        return " · $sign$rendered"
    }

    private fun dateResult(parsed: TimeQueryParser.Parsed.DateOffset): SearchResult {
        val target: LocalDate = parsed.base.plus(parsed.amount, parsed.unit)
        val rendered = target.format(DATE_FORMAT)
        val weekday = target.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            java.util.Locale.getDefault(),
        )

        return answer(
            resultId = "time:date:$target",
            title = rendered,
            subtitle = weekday,
            icon = LumenIcon.Calendar,
            copyValue = target.toString(),
        )
    }

    private fun answer(
        resultId: String,
        title: String,
        subtitle: String,
        icon: LumenIcon,
        copyValue: String,
    ) = SearchResult(
        id = ResultId(resultId),
        providerId = id,
        title = title,
        subtitle = subtitle,
        icon = IconSource.Vector(icon),
        category = ResultCategory.Answer,
        score = 1f,
        rankingKey = "time",
        triggerable = false,
        actions = ResultActions(
            primary = ResultAction(
                id = "copy",
                label = TextValue.Res(R.string.action_copy),
                icon = IconSource.Vector(LumenIcon.Copy),
                kind = ActionKind.Copy,
                invoke = { context ->
                    context.copyToClipboard("Time", copyValue)
                    ActionOutcome.Message(TextValue.Res(R.string.copied))
                },
            ),
        ),
    )

    private companion object {
        val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    }
}
