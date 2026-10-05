package com.abrarshakhi.lumen.core.domain.permission

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
        fun applicable(permissions: List<AppPermission>, sdkInt: Int): List<AppPermission> =
            permissions.filter { it.appliesTo(sdkInt) }
    }
}
