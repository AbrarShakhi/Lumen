package com.abrarshakhi.lumen.core.domain.permission

/**
 * A runtime permission a provider may need, with the SDK range over which it applies.
 *
 * The range matters: storage access split at API 33, so the correct request on the
 * Android 12 test device is [ReadExternalStorage], while the same provider on a modern
 * device must ask for the granular media permissions instead. Encoding that as data and
 * resolving it against `Build.VERSION.SDK_INT` at runtime means neither device is
 * hard-coded, and [appliesTo] can be unit-tested at both levels without either device.
 */
enum class AppPermission(
    val manifestName: String,
    val minSdk: Int = 0,
    val maxSdk: Int = Int.MAX_VALUE,
) {
    ReadContacts("android.permission.READ_CONTACTS"),
    ReadCalendar("android.permission.READ_CALENDAR"),
    CallPhone("android.permission.CALL_PHONE"),
    ReadExternalStorage("android.permission.READ_EXTERNAL_STORAGE", maxSdk = 32),
    ReadMediaImages("android.permission.READ_MEDIA_IMAGES", minSdk = 33),
    ReadMediaVideo("android.permission.READ_MEDIA_VIDEO", minSdk = 33),
    ReadMediaAudio("android.permission.READ_MEDIA_AUDIO", minSdk = 33);

    fun appliesTo(sdkInt: Int): Boolean = sdkInt in minSdk..maxSdk

    companion object {
        /** The subset of [permissions] that is meaningful on [sdkInt]. */
        fun applicable(permissions: List<AppPermission>, sdkInt: Int): List<AppPermission> =
            permissions.filter { it.appliesTo(sdkInt) }
    }
}
