package com.abrarshakhi.lumen.core.data.preferences

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Typed settings persisted as JSON via DataStore.
 *
 * JSON rather than Proto: Proto is the textbook choice, but it would add the protobuf
 * Gradle plugin and `.proto` codegen to a brand-new AGP 9 build for no benefit over a
 * `@Serializable` class, given kotlinx-serialization is already applied for Navigation 3.
 * Preferences DataStore was rejected for the opposite reason — no type safety, and
 * defaults scattered across every read site.
 */
internal class UserPreferencesSerializer(
    private val json: Json,
) : Serializer<UserPreferencesDto> {

    override val defaultValue = UserPreferencesDto()

    override suspend fun readFrom(input: InputStream): UserPreferencesDto =
        try {
            json.decodeFromString(
                UserPreferencesDto.serializer(),
                input.readBytes().decodeToString(),
            )
        } catch (cause: SerializationException) {
            // Signals DataStore to invoke the corruption handler rather than crash.
            throw CorruptionException("Settings file is unreadable", cause)
        }

    override suspend fun writeTo(t: UserPreferencesDto, output: OutputStream) {
        output.write(json.encodeToString(UserPreferencesDto.serializer(), t).encodeToByteArray())
    }
}

/**
 * App-scoped settings holder.
 *
 * Exposed as a [StateFlow] and injected as a singleton, read directly by composables that
 * need it. Deliberately *not* fronted by a ViewModel: theme and font scale are app-wide,
 * outlive any one screen, and routing them through a screen ViewModel is how a
 * "MainViewModel" grows into a god object.
 */
class DataStoreUserPreferencesRepository(
    context: Context,
    private val scope: CoroutineScope,
    json: Json = DefaultJson,
) : UserPreferencesRepository {

    private val dataStore: DataStore<UserPreferencesDto> = DataStoreFactory.create(
        serializer = UserPreferencesSerializer(json),
        // A corrupt settings file must never brick the launcher; fall back to defaults.
        corruptionHandler = ReplaceFileCorruptionHandler { UserPreferencesDto() },
        scope = scope,
        produceFile = { File(context.filesDir, "datastore/$FILE_NAME") },
    )

    override val preferences: StateFlow<UserPreferences> = dataStore.data
        .catch { emit(UserPreferencesDto()) }
        .map { it.toDomain() }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = UserPreferences.Default,
        )

    override suspend fun update(transform: (UserPreferences) -> UserPreferences) {
        dataStore.updateData { stored ->
            UserPreferencesDto.fromDomain(transform(stored.toDomain()))
        }
    }

    companion object {
        private const val FILE_NAME = "user_preferences.json"

        internal val DefaultJson = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
}
