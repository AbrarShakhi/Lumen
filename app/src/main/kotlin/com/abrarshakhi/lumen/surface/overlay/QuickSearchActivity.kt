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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.util.Consumer
import com.abrarshakhi.lumen.app.LaunchActions
import com.abrarshakhi.lumen.app.LaunchEvent
import com.abrarshakhi.lumen.app.LaunchRequest
import com.abrarshakhi.lumen.app.navigation.AppRouteKey
import com.abrarshakhi.lumen.app.ui.AppRoot
import com.abrarshakhi.lumen.feature.search.SearchPresentation

class QuickSearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var query by remember { mutableStateOf(intent.prefilledQuery()) }
            var serial by rememberSaveable { mutableIntStateOf(0) }

            DisposableEffect(Unit) {
                val listener = Consumer<Intent> { newIntent ->
                    setIntent(newIntent)
                    query = newIntent.prefilledQuery()
                    serial += 1
                }
                addOnNewIntentListener(listener)
                onDispose { removeOnNewIntentListener(listener) }
            }

            AppRoot(
                launch = LaunchEvent(
                    request = LaunchRequest(
                        route = AppRouteKey.Search,
                        prefilledQuery = query,
                        focusInput = true,
                    ),
                    serial = serial,
                ),
                onFinish = ::dismiss,
                presentation = SearchPresentation.Panel,
            )
        }
    }

    private fun dismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
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
        fun intent(context: Context, prefilledQuery: String? = null): Intent =
            Intent(context, QuickSearchActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                prefilledQuery?.let { putExtra(LaunchActions.EXTRA_QUERY, it) }
            }
    }
}
