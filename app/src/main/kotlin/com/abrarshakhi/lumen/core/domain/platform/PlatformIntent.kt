package com.abrarshakhi.lumen.core.domain.platform

sealed interface PlatformIntent {

    data class Component(val packageName: String, val activityName: String) : PlatformIntent

    data class ViewUri(val uri: String) : PlatformIntent

    data class ViewDocument(val uri: String, val mimeType: String?) : PlatformIntent

    data class Dial(val phoneNumber: String) : PlatformIntent

    data class Call(val phoneNumber: String) : PlatformIntent

    data class Sms(val phoneNumber: String, val body: String? = null) : PlatformIntent

    data class Share(val text: String, val mimeType: String = "text/plain") : PlatformIntent

    data class SystemSetting(val action: String, val packageName: String? = null) : PlatformIntent

    data class AppDetails(val packageName: String) : PlatformIntent

    data class Uninstall(val packageName: String) : PlatformIntent

    data class AppShortcut(val packageName: String, val shortcutId: String) : PlatformIntent

    data class CalendarEvent(
        val eventId: Long,
        val beginMillis: Long,
        val endMillis: Long,
    ) : PlatformIntent
}
