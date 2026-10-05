package com.abrarshakhi.lumen.core.data.secret

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.abrarshakhi.lumen.core.data.crypto.KeystoreSecretCipher
import com.abrarshakhi.lumen.core.domain.secret.SecretId
import com.abrarshakhi.lumen.core.domain.secret.SecretStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

@Serializable
internal data class SecretsEnvelope(
    val secrets: Map<String, String> = emptyMap(),
)

internal class SecretsEnvelopeSerializer(private val json: Json) : Serializer<SecretsEnvelope> {

    override val defaultValue = SecretsEnvelope()

    override suspend fun readFrom(input: InputStream): SecretsEnvelope =
        try {
            json.decodeFromString(SecretsEnvelope.serializer(), input.readBytes().decodeToString())
        } catch (cause: SerializationException) {
            throw CorruptionException("Secrets file is unreadable", cause)
        }

    override suspend fun writeTo(t: SecretsEnvelope, output: OutputStream) {
        output.write(json.encodeToString(SecretsEnvelope.serializer(), t).encodeToByteArray())
    }
}

class DataStoreSecretStore(
    context: Context,
    scope: CoroutineScope,
    private val cipher: KeystoreSecretCipher = KeystoreSecretCipher(),
    json: Json = Json,
) : SecretStore {

    private val dataStore: DataStore<SecretsEnvelope> = DataStoreFactory.create(
        serializer = SecretsEnvelopeSerializer(json),
        corruptionHandler = ReplaceFileCorruptionHandler { SecretsEnvelope() },
        scope = scope,
        produceFile = { File(context.filesDir, "datastore/$FILE_NAME") },
    )

    override suspend fun put(id: SecretId, value: String) {
        val encrypted = cipher.encrypt(value)
        dataStore.updateData { it.copy(secrets = it.secrets + (id.value to encrypted)) }
    }

    override suspend fun get(id: SecretId): String? {
        val envelope = dataStore.data.first().secrets[id.value] ?: return null
        return try {
            cipher.decrypt(envelope)
        } catch (_: KeystoreSecretCipher.DecryptionFailed) {
            clear(id)
            null
        }
    }

    override suspend fun clear(id: SecretId) {
        dataStore.updateData { it.copy(secrets = it.secrets - id.value) }
    }

    override suspend fun has(id: SecretId): Boolean =
        dataStore.data.first().secrets.containsKey(id.value)

    private companion object {
        const val FILE_NAME = "secrets.json"
    }
}
