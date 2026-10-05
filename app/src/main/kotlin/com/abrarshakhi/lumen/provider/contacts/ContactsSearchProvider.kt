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

/**
 * Searches the device's contacts.
 *
 * Note what this class does *not* contain: any permission check, any `Context`, any
 * knowledge of the search engine. It declares `READ_CONTACTS` in its metadata and the gate
 * does the rest — which is why adding it required no change to the engine, the ViewModel,
 * or the search screen.
 *
 * A small debounce, unlike the apps provider: the first query after permission is granted
 * hits the ContentResolver, so it is worth not firing that on every keystroke.
 */
class ContactsSearchProvider(
    private val contacts: ContactRepository,
    private val installedApps: InstalledAppsProbe,
) : SnapshotSearchProvider() {

    override val id = ProviderId("contacts")

    override val metadata = ProviderMetadata(
        displayName = TextValue.Res(R.string.provider_contacts),
        category = ResultCategory.Contact,
        order = 20,
        requiredPermissions = listOf(AppPermission.ReadContacts),
        timeout = 600.milliseconds,
        debounce = 80.milliseconds,
        // Single letters match far too many people to be useful.
        minQueryLength = 2,
    )

    override suspend fun warmUp() {
        // Only pre-loads if permission already happens to be granted; otherwise the query
        // returns nothing and the first real search loads it.
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

    /** Shows the number, plus a hint when there are others behind the long-press sheet. */
    private fun Contact.formatSubtitle(number: PhoneNumber): String {
        val base = listOfNotNull(number.label, number.number).joinToString(" · ")
        val extra = phoneNumbers.size - 1
        return if (extra > 0) "$base  +$extra" else base
    }

    private fun Contact.buildActions(number: PhoneNumber?): ResultActions {
        if (number == null) {
            // A contact with no number can still be opened in the contacts app.
            return ResultActions(primary = openContactAction())
        }

        val messaging = MessagingApp.entries
            .filter { installedApps.isInstalled(it.packageName) }
            .map { app ->
                ResultAction(
                    id = "chat-${app.name.lowercase()}",
                    label = TextValue.Raw(app.displayName),
                    // Distinct from SMS: two identical icons sitting side by side in a row
                    // tells the user nothing about which is which.
                    icon = IconSource.Vector(LumenIcon.Chat),
                    kind = ActionKind.Message,
                    invoke = {
                        ActionOutcome.Launch(PlatformIntent.ViewUri(app.chatUri(number.digits)))
                    },
                )
            }

        return ResultActions(
            // Dial rather than Call: it opens the dialer pre-filled instead of placing the
            // call immediately, so a mistap is recoverable and no CALL_PHONE grant is needed.
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
