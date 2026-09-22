package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

/** A phone number belonging to a contact. */
data class PhoneNumber(
    val number: String,
    /** "Mobile", "Work" — already localised by the platform. */
    val label: String?,
) {
    /** Digits only, for building `wa.me` / `t.me` links. */
    val digits: String get() = number.filter { it.isDigit() || it == '+' }
}

data class Contact(
    val id: String,
    val lookupKey: String,
    val displayName: String,
    val searchable: SearchableText,
    val phoneNumbers: List<PhoneNumber>,
    val photoUri: String?,
) {
    val primaryNumber: PhoneNumber? get() = phoneNumbers.firstOrNull()
}

/**
 * Access to the device's contacts.
 *
 * Results are cached in memory rather than indexed into Room: contacts change rarely
 * within a session, a `ContentResolver` query per keystroke would be far too slow, and
 * duplicating the system's own database on disk buys nothing.
 */
interface ContactRepository {
    /** Cached contacts, loading them on first use. Empty when permission is not granted. */
    suspend fun contacts(): List<Contact>

    /** Drops the cache so the next read re-queries — e.g. after permission is granted. */
    fun invalidate()
}
