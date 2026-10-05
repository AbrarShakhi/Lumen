package com.abrarshakhi.lumen.core.domain.platform

/**
 * A serialisable description of something to launch.
 *
 * Providers return these instead of Android `Intent`s, for three reasons: the domain layer
 * stays free of `android.*`; actions become unit-testable by asserting on data; and because
 * this is data rather than a lambda, other processes — the Quick Settings tile, the Glance
 * widget — can carry one across a process boundary and hand it back to be launched.
 *
 * This is a closed set over Android's intent surface, which is stable, rather than over
 * providers, which are not. Adding a provider never adds a case here.
 */
sealed interface PlatformIntent {

    /** Launch an activity by explicit component, e.g. an installed app. */
    data class Component(val packageName: String, val activityName: String) : PlatformIntent

    /** `ACTION_VIEW` on an arbitrary URI. */
    data class ViewUri(val uri: String) : PlatformIntent

    /** `ACTION_VIEW` on a content/document URI, granting read permission. */
    data class ViewDocument(val uri: String, val mimeType: String?) : PlatformIntent

    /** `ACTION_DIAL` — opens the dialer pre-filled, without placing the call. */
    data class Dial(val phoneNumber: String) : PlatformIntent

    /** `ACTION_CALL` — places the call directly. Requires `CALL_PHONE`. */
    data class Call(val phoneNumber: String) : PlatformIntent

    /** `ACTION_SENDTO` over `smsto:`. */
    data class Sms(val phoneNumber: String, val body: String? = null) : PlatformIntent

    data class Share(val text: String, val mimeType: String = "text/plain") : PlatformIntent

    /** A `Settings.ACTION_*` screen. [action] is the fully-qualified action string. */
    data class SystemSetting(val action: String, val packageName: String? = null) : PlatformIntent

    /** The system app-details page for a package. */
    data class AppDetails(val packageName: String) : PlatformIntent

    data class Uninstall(val packageName: String) : PlatformIntent

    /**
     * Opens a calendar event.
     *
     * Needs the instance's start and end alongside the id: a recurring event has one id but
     * many occurrences, and without the times the calendar app opens the wrong one.
     */
    /**
     * Launches an app shortcut.
     *
     * Not an Intent at the platform level — `LauncherApps.startShortcut` is the only way,
     * and it works solely while Lumen is the launcher.
     */
    data class AppShortcut(val packageName: String, val shortcutId: String) : PlatformIntent

    data class CalendarEvent(
        val eventId: Long,
        val beginMillis: Long,
        val endMillis: Long,
    ) : PlatformIntent
}
