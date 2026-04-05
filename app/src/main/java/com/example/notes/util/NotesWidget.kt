package com.example.notes.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.unit.ColorProvider
import com.example.notes.MainActivity
import com.example.notes.R

class NotesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent(context)
            }
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        Row(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(28.dp)
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Кнопка Заметка
            WidgetItem(
                iconRes = R.drawable.ic_notes_widget,
                contentDescription = "Заметка",
                action = actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        data = Uri.parse("notesapp://add_note")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                ),
                modifier = GlanceModifier.defaultWeight()
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Кнопка Цитата
            WidgetItem(
                iconRes = R.drawable.ic_format_quote_widget,
                contentDescription = "Цитата",
                action = actionStartActivity(
                    Intent(context, MainActivity::class.java).apply {
                        data = Uri.parse("notesapp://add_quote")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                ),
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }

    @Composable
    private fun WidgetItem(
        iconRes: Int,
        contentDescription: String,
        action: Action,
        modifier: GlanceModifier = GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .background(GlanceTheme.colors.secondaryContainer)
                .cornerRadius(20.dp)
                .clickable(action),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(iconRes),
                contentDescription = contentDescription,
                modifier = GlanceModifier.size(24.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSecondaryContainer)
            )
        }
    }
}
