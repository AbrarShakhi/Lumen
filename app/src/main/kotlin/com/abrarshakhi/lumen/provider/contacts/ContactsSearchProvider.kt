package com.abrarshakhi.lumen.provider.contacts

import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.core.domain.match.FuzzyMatcher
import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.platform.PlatformIntent
import com.abrarshakhi.lumen.core.domain.repository.Contact
import com.abrarshakhi.lumen.core.domain.repository.ContactRepository
import com.abrarshakhi.lumen.core.domain.repository.InstalledAppsProbe
import com.abrarshakhi.lumen.core.domain.repository.MessagingApp
import com.abrarshakhi.lumen.core.domain.repository.PhoneNumber
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
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class ContactsSearchProvider(
    private val contacts: ContactRepository,
    private val installedApps: InstalledAppsProbe,
) : SnapshotSearchProvider() {

    override val id = ProviderId("contacts")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_contacts),
        category = ResultCategory.Contact,
        icon = LumenIcon.Contact,
        order = 20,
        requiredPermissions = listOf(AppPermission.ReadContacts),
        timeout = 600.milliseconds,
        debounce = 80.milliseconds,
        minQueryLength = 2,
    )

    override suspend fun warmUp() {
        contacts.contacts()
    }

    override suspend fun runSearch(query: SearchQuery): List<SearchResult> {
        val terms = query.normalizedTerms
        if (terms.isBlank()) return emptyList()

        return contacts.contacts()
            .mapNotNull { contact ->
                val match = FuzzyMatcher.score(terms, contact.searchable) ?: return@mapNotNull null
                contact.toResult(match.score, match.ranges)
            }
            .sortedByDescending { it.score }
            .take(MAX_RESULTS)
    }

    private fun Contact.toResult(score: Float, matches: List<IntRange>): SearchResult {
        val number = primaryNumber
        return SearchResult(
            id = ResultId("contact:$id"),
            providerId = this@ContactsSearchProvider.id,
            title = displayName,
            subtitle = number?.let { formatSubtitle(it) },
            icon = photoUri?.let { IconSource.ContactPhoto(it, displayName.first()) }
                ?: IconSource.Letter(displayName),
            category = ResultCategory.Contact,
            score = score,
            titleMatches = matches,
            rankingKey = "contact:$id",
            actions = buildActions(number),
        )
    }

    private fun Contact.formatSubtitle(number: PhoneNumber): String {
        val base = listOfNotNull(number.label, number.number).joinToString(" · ")
        val extra = phoneNumbers.size - 1
        return if (extra > 0) "$base  +$extra" else base
    }

    private fun Contact.buildActions(number: PhoneNumber?): ResultActions {
        if (number == null) {
            return ResultActions(primary = openContactAction())
        }

        val messaging = MessagingApp.entries
            .filter { installedApps.isInstalled(it.packageName) }
            .map { app ->
                ResultAction(
                    id = "chat-${app.name.lowercase()}",
                    label = TextValue.Raw(app.displayName),
                    icon = IconSource.Vector(LumenIcon.Chat),
                    kind = ActionKind.Message,
                    invoke = {
                        ActionOutcome.Launch(PlatformIntent.ViewUri(app.chatUri(number.digits)))
                    },
                )
            }

        return ResultActions(
            primary = ResultAction(
                id = ACTION_DIAL,
                label = TextValue.Res(R.string.action_call),
                icon = IconSource.Vector(LumenIcon.Phone),
                kind = ActionKind.Call,
                invoke = { ActionOutcome.Launch(PlatformIntent.Dial(number.number)) },
            ),
            secondary = buildList {
                add(
                    ResultAction(
                        id = ACTION_SMS,
                        label = TextValue.Res(R.string.action_message),
                        icon = IconSource.Vector(LumenIcon.Message),
                        kind = ActionKind.Message,
                        invoke = { ActionOutcome.Launch(PlatformIntent.Sms(number.number)) },
                    ),
                )
                addAll(messaging)
                add(openContactAction())
            },
        )
    }

    private fun Contact.openContactAction() = ResultAction(
        id = ACTION_OPEN,
        label = TextValue.Res(R.string.action_open_contact),
        icon = IconSource.Vector(LumenIcon.Contact),
        kind = ActionKind.Open,
        invoke = {
            ActionOutcome.Launch(
                PlatformIntent.ViewUri("content://com.android.contacts/contacts/$id"),
            )
        },
    )

    private companion object {
        const val MAX_RESULTS = 16
        const val ACTION_DIAL = "dial"
        const val ACTION_SMS = "sms"
        const val ACTION_OPEN = "open-contact"
    }
}
