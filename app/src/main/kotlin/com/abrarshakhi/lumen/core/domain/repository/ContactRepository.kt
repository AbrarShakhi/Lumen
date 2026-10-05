package com.abrarshakhi.lumen.core.domain.repository

import com.abrarshakhi.lumen.core.domain.match.SearchableText

data class PhoneNumber(
    val number: String,
    val label: String?,
) {
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

interface ContactRepository {
    suspend fun contacts(): List<Contact>

    fun invalidate()
}
