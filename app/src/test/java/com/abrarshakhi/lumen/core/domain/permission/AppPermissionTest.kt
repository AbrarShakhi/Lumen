package com.abrarshakhi.lumen.core.domain.permission

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Storage permissions split at API 33. The test device runs API 31 and a modern device runs
 * 36+, so only one of those paths can ever be exercised by hand — encoding the rule as data
 * is what lets both be verified here.
 */
class AppPermissionTest {

    @Test
    fun `legacy storage permission applies only up to API 32`() {
        assertTrue(AppPermission.ReadExternalStorage.appliesTo(31), "needed on the Android 12 test device")
        assertTrue(AppPermission.ReadExternalStorage.appliesTo(32))
        assertFalse(AppPermission.ReadExternalStorage.appliesTo(33), "superseded by granular media permissions")
    }

    @Test
    fun `granular media permissions apply only from API 33`() {
        assertFalse(AppPermission.ReadMediaImages.appliesTo(31))
        assertTrue(AppPermission.ReadMediaImages.appliesTo(33))
        assertTrue(AppPermission.ReadMediaVideo.appliesTo(36))
    }

    @Test
    fun `a file provider asks for exactly one storage scheme per device`() {
        val declared = listOf(
            AppPermission.ReadExternalStorage,
            AppPermission.ReadMediaImages,
            AppPermission.ReadMediaVideo,
            AppPermission.ReadMediaAudio,
        )

        assertEquals(
            listOf(AppPermission.ReadExternalStorage),
            AppPermission.applicable(declared, sdkInt = 31),
            "on Android 12 only the legacy permission should be requested",
        )
        assertEquals(
            listOf(AppPermission.ReadMediaImages, AppPermission.ReadMediaVideo, AppPermission.ReadMediaAudio),
            AppPermission.applicable(declared, sdkInt = 34),
            "on Android 14 only the granular permissions should be requested",
        )
    }

    @Test
    fun `permissions without an sdk range always apply`() {
        assertTrue(AppPermission.ReadContacts.appliesTo(31))
        assertTrue(AppPermission.ReadContacts.appliesTo(99))
    }
}
