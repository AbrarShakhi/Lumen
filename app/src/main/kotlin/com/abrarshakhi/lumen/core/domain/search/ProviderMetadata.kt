package com.abrarshakhi.lumen.core.domain.search

import com.abrarshakhi.lumen.core.domain.permission.AppPermission
import com.abrarshakhi.lumen.core.domain.permission.PlatformCapability
import com.abrarshakhi.lumen.core.domain.text.TextValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class ProviderMetadata(
    val displayName: TextValue,
    val category: ResultCategory,
    val order: Int,
    val requiredPermissions: List<AppPermission> = emptyList(),
    val requiredCapabilities: List<PlatformCapability> = emptyList(),
    val optionalCapabilities: List<PlatformCapability> = emptyList(),
    val timeout: Duration = 800.milliseconds,
    val debounce: Duration = Duration.ZERO,
    val minQueryLength: Int = 1,
    val requiresExplicitTrigger: Boolean = false,
    val defaultEnabled: Boolean = true,
)
