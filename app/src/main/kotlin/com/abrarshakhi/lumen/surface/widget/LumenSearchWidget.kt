package com.abrarshakhi.lumen.surface.widget

import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.ColorFilter
import androidx.glance.appwidget.action.actionStartActivity
import android.content.Context
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.surface.overlay.QuickSearchActivity

/**
 * A home-screen search bar that opens the Lumen panel.
 *
 * Glance runs in the launcher's process with its own composable dialect, so none of the
 * app's Compose UI can be reused here — this is a deliberately minimal affordance whose
 * only job is to launch the real surface.
 */
class LumenSearchWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // GlanceTheme picks up the system dynamic colours, so the widget matches the
            // launcher's wallpaper the way other Material You widgets do.
            GlanceTheme {
                SearchBar(context)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun SearchBar(context: Context) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(56.dp)
                .cornerRadius(28.dp)
                .background(GlanceTheme.colors.surfaceVariant)
                .clickable(actionStartActivity(QuickSearchActivity.intent(context)))
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_search),
                contentDescription = null,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant),
                modifier = GlanceModifier.size(22.dp),
            )
            Spacer(GlanceModifier.width(14.dp))
            Text(
                text = context.getString(R.string.widget_hint),
                style = TextStyle(color = GlanceTheme.colors.onSurfaceVariant),
            )
        }
    }
}

class LumenSearchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LumenSearchWidget()
}
