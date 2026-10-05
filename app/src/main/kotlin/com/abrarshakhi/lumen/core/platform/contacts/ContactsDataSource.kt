package com.abrarshakhi.lumen.core.platform.contacts

import android.content.Context
import android.provider.ContactsContract
import com.abrarshakhi.lumen.core.domain.match.SearchableText
import com.abrarshakhi.lumen.core.domain.repository.Contact
import com.abrarshakhi.lumen.core.domain.repository.PhoneNumber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsDataSource(
    private val context: Context,
) {

    suspend fun loadContacts(): List<Contact>? = withContext(Dispatchers.IO) {
        runCatching { query() }.getOrNull()
    }

    private fun query(): List<Contact> {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.LABEL,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
        )

        val builders = LinkedHashMap<String, ContactBuilder>()

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} COLLATE NOCASE ASC",
        ).let { it ?: error("Contacts query returned no cursor") }.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val lookupColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY)
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY)
            val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            val labelColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL)
            val photoColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

            while (cursor.moveToNext()) {
                val contactId = cursor.getString(idColumn) ?: continue
                val name = cursor.getString(nameColumn)?.trim().orEmpty()
                if (name.isEmpty()) continue

                val number = cursor.getString(numberColumn)?.trim().orEmpty()
                if (number.isEmpty()) continue

                val builder = builders.getOrPut(contactId) {
                    ContactBuilder(
                        id = contactId,
                        lookupKey = cursor.getString(lookupColumn).orEmpty(),
                        displayName = name,
                        photoUri = cursor.getString(photoColumn),
                    )
                }

                val label = ContactsContract.CommonDataKinds.Phone.getTypeLabel(
                    context.resources,
                    cursor.getInt(typeColumn),
                    cursor.getString(labelColumn),
                ).toString()

                if (builder.numbers.none { it.digits == PhoneNumber(number, null).digits }) {
                    builder.numbers += PhoneNumber(number, label.takeIf { it.isNotBlank() })
                }
            }
        }

        return builders.values.map { it.build() }
    }

    private class ContactBuilder(
        val id: String,
        val lookupKey: String,
        val displayName: String,
        val photoUri: String?,
    ) {
        val numbers = mutableListOf<PhoneNumber>()

        fun build() = Contact(
            id = id,
            lookupKey = lookupKey,
            displayName = displayName,
            searchable = SearchableText.of(displayName),
            phoneNumbers = numbers.toList(),
            photoUri = photoUri,
        )
    }
}
