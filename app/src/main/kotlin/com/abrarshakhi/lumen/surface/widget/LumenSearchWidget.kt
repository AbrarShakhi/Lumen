package com.abrarshakhi.lumen.surface.widget

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.app.LaunchIntents
import com.abrarshakhi.lumen.surface.overlay.QuickSearchActivity

class LumenSearchWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(COMPACT, WIDE, TALL))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                SearchWidgetContent()
            }
        }
    }

    private companion object {
        val COMPACT = DpSize(140.dp, 56.dp)
        val WIDE = DpSize(260.dp, 56.dp)
        val TALL = DpSize(260.dp, 130.dp)
    }
}

class LumenSearchWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LumenSearchWidget()
}

private enum class WidgetShortcut(
    @param:DrawableRes val icon: Int,
    @param:StringRes val label: Int,
) {
    NewNote(R.drawable.ic_widget_edit_note, R.string.quick_new_note),
    Notes(R.drawable.ic_widget_notes, R.string.quick_notes),
    Settings(R.drawable.ic_widget_settings, R.string.quick_settings),
}

private fun WidgetShortcut.action(context: Context): Action = actionStartActivity(
    when (this) {
        WidgetShortcut.NewNote -> LaunchIntents.newNote(context)
        WidgetShortcut.Notes -> LaunchIntents.notes(context)
        WidgetShortcut.Settings -> LaunchIntents.settings(context)
    },
)

@Composable
private fun SearchWidgetContent() {
    val context = LocalContext.current
    val size = LocalSize.current
    val showShortcutRow = size.height >= SHORTCUT_ROW_MIN_HEIGHT
    val showInlineAction = !showShortcutRow && size.width >= INLINE_ACTION_MIN_WIDTH

    Column(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SearchPill(context = context, showInlineAction = showInlineAction)
        if (showShortcutRow) {
            Spacer(GlanceModifier.height(8.dp))
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                WidgetShortcut.entries.forEachIndexed { index, shortcut ->
                    if (index > 0) Spacer(GlanceModifier.width(8.dp))
                    ShortcutButton(
                        context = context,
                        shortcut = shortcut,
                        modifier = GlanceModifier.defaultWeight(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchPill(context: Context, showInlineAction: Boolean) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(PILL_HEIGHT)
            .cornerRadius(PILL_HEIGHT / 2)
            .background(GlanceTheme.colors.primaryContainer)
            .clickable(actionStartActivity(QuickSearchActivity.intent(context)))
            .padding(start = 10.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(R.mipmap.ic_launcher_round),
            contentDescription = null,
            modifier = GlanceModifier.size(36.dp),
        )
        Spacer(GlanceModifier.width(12.dp))
        Text(
            text = context.getString(R.string.widget_hint),
            style = TextStyle(
                color = GlanceTheme.colors.onPrimaryContainer,
                fontSize = 16.sp,
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight(),
        )
        if (showInlineAction) {
            Image(
                provider = ImageProvider(WidgetShortcut.NewNote.icon),
                contentDescription = context.getString(WidgetShortcut.NewNote.label),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer),
                modifier = GlanceModifier
                    .size(40.dp)
                    .cornerRadius(20.dp)
                    .clickable(WidgetShortcut.NewNote.action(context))
                    .padding(9.dp),
            )
        }
    }
}

@Composable
private fun ShortcutButton(
    context: Context,
    shortcut: WidgetShortcut,
    modifier: GlanceModifier,
) {
    Column(
        modifier = modifier
            .height(SHORTCUT_HEIGHT)
            .cornerRadius(20.dp)
            .background(GlanceTheme.colors.secondaryContainer)
            .clickable(shortcut.action(context)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            provider = ImageProvider(shortcut.icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer),
            modifier = GlanceModifier.size(22.dp),
        )
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = context.getString(shortcut.label),
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            ),
            maxLines = 1,
        )
    }
}

private val PILL_HEIGHT = 56.dp
private val SHORTCUT_HEIGHT = 64.dp
private val SHORTCUT_ROW_MIN_HEIGHT = 120.dp
private val INLINE_ACTION_MIN_WIDTH = 250.dp
