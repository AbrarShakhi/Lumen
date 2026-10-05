package com.abrarshakhi.lumen.app

import android.app.Application
import com.abrarshakhi.lumen.BuildConfig
import com.abrarshakhi.lumen.app.di.LumenModules
import com.abrarshakhi.lumen.core.data.di.ApplicationScopeQualifier
import com.abrarshakhi.lumen.core.domain.search.SearchProviderRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class LumenApp : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.ERROR else Level.NONE)
            androidContext(this@LumenApp)
            modules(LumenModules.all)
        }

        warmUpProviders()
    }

    /**
     * Builds each provider's index off the main thread before the user types.
     *
     * Failures are swallowed per provider: a provider that cannot warm up should degrade
     * to empty results, never prevent the app from starting.
     */
    private fun warmUpProviders() {
        val scope = get<CoroutineScope>(ApplicationScopeQualifier)
        val registry = get<SearchProviderRegistry>()
        scope.launch {
            registry.providers.forEach { provider ->
                runCatching { provider.warmUp() }
            }
        }
    }
}
