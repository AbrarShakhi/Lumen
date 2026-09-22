package com.abrarshakhi.lumen.surface.overlay

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.abrarshakhi.lumen.app.LaunchActions
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.app.ui.AppRoot
import com.abrarshakhi.lumen.feature.search.SearchPresentation

/**
 * The summon-from-anywhere surface: a floating search panel over whatever is on screen.
 *
 * Implemented as a **translucent Activity rather than a `SYSTEM_ALERT_WINDOW` overlay**.
 * The two look identical to the user, but the overlay route costs a special permission the
 * user must grant in Settings, and on ColorOS it additionally needs the OEM's own
 * "display over other apps" and auto-launch toggles — after which Android 12's
 * background-activity-start restrictions still apply. A translucent activity needs none of
 * that and cannot be killed by OEM power management.
 *
 * Every other launch surface (Quick Settings tile, home-screen widget) is a thin launcher
 * into this activity, which is why it accepts the same intent extras as the main one.
 */
class QuickSearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var query by remember { mutableStateOf(intent.prefilledQuery()) }

            // singleTask means a second summon arrives here rather than through onCreate;
            // without this, re-summoning Lumen would silently reuse the previous query.
            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent -> query = newIntent.prefilledQuery() }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            AppRoot(
                startRoute = AppRouteKey.Search,
                onFinish = ::dismiss,
                initialQuery = query,
                autoFocus = true,
                presentation = SearchPresentation.Panel,
            )
        }
    }

    /**
     * Closes without the default activity transition.
     *
     * A summoned panel should disappear as directly as it appeared; the standard close
     * animation reads as leaving an app, which this is not.
     */
    private fun dismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Must be set before finish() for the close transition to be picked up.
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
            finish()
        } else {
            finish()
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

    private fun Intent?.prefilledQuery(): String? =
        this?.getStringExtra(LaunchActions.EXTRA_QUERY)?.trim()?.takeIf { it.isNotEmpty() }

    companion object {
        /**
         * An intent that summons the panel.
         *
         * Exposed here so the tile and widget surfaces have one place to depend on rather
         * than each hand-rolling component names and flags.
         */
        fun intent(context: Context, prefilledQuery: String? = null): Intent =
            Intent(context, QuickSearchActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                prefilledQuery?.let { putExtra(LaunchActions.EXTRA_QUERY, it) }
            }
    }
}
