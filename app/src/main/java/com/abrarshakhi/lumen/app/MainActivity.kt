package com.abrarshakhi.lumen.app

import android.content.Intent
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
import com.abrarshakhi.lumen.app.ui.AppRoot

/**
 * Lumen's main surface.
 *
 * Every entry point — launcher icon, assistant gesture, Quick Settings tile, widget —
 * arrives here, and where it lands is decided by [parseLaunchIntent]. Because the activity
 * is `singleTask`, a second launch is delivered through `onNewIntent` rather than
 * `onCreate`; handling only the latter would mean summoning Lumen again silently reused
 * the previous query.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var request by remember { mutableStateOf(intent.toLaunchRequest()) }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    request = newIntent.toLaunchRequest()
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            AppRoot(
                startRoute = request.route,
                onFinish = { finish() },
                initialQuery = request.prefilledQuery,
                autoFocus = request.focusInput,
            )
        }
    }

    private fun Intent?.toLaunchRequest(): LaunchRequest = parseLaunchIntent(
        action = this?.action,
        query = this?.getStringExtra(LaunchActions.EXTRA_QUERY),
        // The onboarding screen does not exist yet, so every launch goes straight to
        // search. This becomes a real preference read once onboarding is built.
        onboardingCompleted = true,
    )
}
