package com.abrarshakhi.lumen.surface.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.abrarshakhi.lumen.R
import com.abrarshakhi.lumen.app.LaunchIntents
import com.abrarshakhi.lumen.core.domain.repository.Note
import com.abrarshakhi.lumen.core.domain.repository.NoteRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class LumenNotesWidget : GlanceAppWidget(), KoinComponent {

    private val notes: NoteRepository by inject()

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val recent = notes.observeAll().map { it.take(MAX_NOTES) }
        val initial = recent.first()

        provideContent {
            val current by remember { recent }.collectAsState(initial = initial)
            GlanceTheme {
                NotesWidgetContent(current)
            }
        }
    }

    private companion object {
        const val MAX_NOTES = 12
    }
}

class LumenNotesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LumenNotesWidget()
}

@Composable
private fun NotesWidgetContent(notes: List<Note>) {
    val context = LocalContext.current

    Scaffold(
        titleBar = {
            TitleBar(
                startIcon = ImageProvider(R.drawable.ic_widget_notes),
                title = context.getString(R.string.notes_title),
                modifier = GlanceModifier.clickable(actionStartActivity(LaunchIntents.notes(context))),
                actions = {
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.ic_widget_add),
                        contentDescription = context.getString(R.string.notes_new),
                        onClick = actionStartActivity(LaunchIntents.newNote(context)),
                        backgroundColor = GlanceTheme.colors.primary,
                        contentColor = GlanceTheme.colors.onPrimary,
                    )
                },
            )
        },
        modifier = GlanceModifier.cornerRadius(24.dp),
    ) {
        if (notes.isEmpty()) {
            EmptyNotes(context)
        } else {
            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                items(notes, itemId = { it.id }) { note ->
                    Column(GlanceModifier.fillMaxWidth()) {
                        NoteItem(context, note)
                        Spacer(GlanceModifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteItem(context: Context, note: Note) {
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(16.dp)
            .background(GlanceTheme.colors.secondaryContainer)
            .clickable(actionStartActivity(LaunchIntents.note(context, note.id)))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = note.displayTitle.ifBlank { context.getString(R.string.notes_untitled) },
            style = TextStyle(
                color = GlanceTheme.colors.onSecondaryContainer,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            ),
            maxLines = 1,
        )
        if (note.preview.isNotBlank() && note.preview != note.displayTitle) {
            Text(
                text = note.preview,
                style = TextStyle(
                    color = GlanceTheme.colors.onSecondaryContainer,
                    fontSize = 13.sp,
                ),
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun EmptyNotes(context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .clickable(actionStartActivity(LaunchIntents.newNote(context))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = context.getString(R.string.widget_notes_empty),
            style = TextStyle(
                color = GlanceTheme.colors.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            ),
        )
    }
}
