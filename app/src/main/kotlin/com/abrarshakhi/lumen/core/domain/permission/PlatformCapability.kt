package com.abrarshakhi.lumen.core.domain.permission

sealed interface PlatformCapability {

    data object DefaultLauncher : PlatformCapability

    data object Network : PlatformCapability

    data object DocumentTreeGrant : PlatformCapability
}

interface PermissionChecker {
    fun isGranted(permission: AppPermission): Boolean

    fun isPermanentlyDenied(permission: AppPermission): Boolean

    val sdkInt: Int
}

interface PermissionRequestRecorder {
    fun recordAsked(permission: AppPermission, canAskAgain: Boolean)
}

interface CapabilityChecker {
    fun has(capability: PlatformCapability): Boolean
}
