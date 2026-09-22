package com.abrarshakhi.lumen.core.domain.permission

/**
 * A platform affordance that is not a runtime permission but still gates functionality.
 *
 * Distinguished from [AppPermission] because these cannot be requested with a permission
 * dialog — each needs its own flow (a role request, a settings deep link, a document
 * picker), and some may be permanently unavailable on a given device.
 */
sealed interface PlatformCapability {

    /**
     * Lumen currently holds the HOME role.
     *
     * The only route to `LauncherApps.getShortcuts()` — Android exposes other apps'
     * shortcuts exclusively to the default launcher, with no partial fallback.
     */
    data object DefaultLauncher : PlatformCapability

    /** A usable network connection. */
    data object Network : PlatformCapability

    /** At least one persisted `ACTION_OPEN_DOCUMENT_TREE` grant, for document search. */
    data object DocumentTreeGrant : PlatformCapability
}

/** Reports whether runtime permissions are granted. Implemented in the platform layer. */
interface PermissionChecker {
    fun isGranted(permission: AppPermission): Boolean

    /** True when the user denied with "don't ask again", so a prompt would be a no-op. */
    fun isPermanentlyDenied(permission: AppPermission): Boolean

    /** The device SDK level, used to resolve [AppPermission.appliesTo]. */
    val sdkInt: Int
}

/**
 * Records that Lumen asked for a permission, and whether it may ask again.
 *
 * Android exposes no direct "permanently denied" signal — only
 * `shouldShowRequestPermissionRationale`, which needs an Activity and is ambiguous before
 * the first ask. Remembering that we asked is what makes the distinction knowable.
 */
interface PermissionRequestRecorder {
    fun recordAsked(permission: AppPermission, canAskAgain: Boolean)
}

/** Reports whether non-permission platform capabilities are currently available. */
interface CapabilityChecker {
    fun has(capability: PlatformCapability): Boolean
}
