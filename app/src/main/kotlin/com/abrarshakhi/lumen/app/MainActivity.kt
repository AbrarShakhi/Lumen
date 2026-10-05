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
        onboardingCompleted = true,
    )
}
