package com.abrarshakhi.lumen.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.abrarshakhi.lumen.app.ui.AppRoot

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var request by remember { mutableStateOf(intent.toLaunchRequest()) }
            var serial by rememberSaveable { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    setIntent(newIntent)
                    request = newIntent.toLaunchRequest()
                    serial += 1
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            AppRoot(
                launch = LaunchEvent(request, serial),
                onFinish = { finish() },
            )
        }
    }

    private fun Intent?.toLaunchRequest(): LaunchRequest = parseLaunchIntent(
        action = this?.action,
        query = this?.getStringExtra(LaunchActions.EXTRA_QUERY),
        noteId = this?.takeIf { it.hasExtra(LaunchActions.EXTRA_NOTE_ID) }
            ?.getLongExtra(LaunchActions.EXTRA_NOTE_ID, 0L),
        onboardingCompleted = true,
    )
}
