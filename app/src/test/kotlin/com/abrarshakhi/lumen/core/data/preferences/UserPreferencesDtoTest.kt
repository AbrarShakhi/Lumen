package com.abrarshakhi.lumen.core.data.preferences

import com.abrarshakhi.lumen.core.domain.preferences.ColorStyle
import com.abrarshakhi.lumen.core.domain.preferences.ThemeAccent
import com.abrarshakhi.lumen.core.domain.preferences.UserPreferences
import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferencesDtoTest {

    @Test
    fun `theme choices survive a round trip`() {
        val preferences = UserPreferences(
            accent = ThemeAccent.Blossom,
            colorStyle = ColorStyle.Vibrant,
            pureBlack = true,
        )

        assertEquals(preferences, UserPreferencesDto.fromDomain(preferences).toDomain())
    }

    @Test
    fun `settings written before theme choices existed load with defaults`() {
        val stored = DataStoreUserPreferencesRepository.DefaultJson.decodeFromString(
            UserPreferencesDto.serializer(),
            """{"themeMode":"Dark","dynamicColor":false}""",
        )

        val preferences = stored.toDomain()
        assertEquals(ThemeAccent.Ember, preferences.accent)
        assertEquals(ColorStyle.TonalSpot, preferences.colorStyle)
        assertEquals(false, preferences.pureBlack)
    }

    @Test
    fun `unknown enum names fall back instead of failing`() {
        val dto = UserPreferencesDto(accent = "Neon", colorStyle = "Rainbow")

        assertEquals(ThemeAccent.Ember, dto.toDomain().accent)
        assertEquals(ColorStyle.TonalSpot, dto.toDomain().colorStyle)
    }
}
