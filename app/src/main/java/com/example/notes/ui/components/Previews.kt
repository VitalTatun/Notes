package com.example.notes.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.notes.data.local.entities.Note
import com.example.notes.ui.screens.NoteItem
import com.example.notes.ui.theme.NotesTheme

@Preview(showBackground = true, widthDp = 360)
@Composable
fun NoteItemPreview() {
    NotesTheme {
        NoteItem(
            note = Note(
                id = 1,
                title = "Заголовок заметки",
                content = "Текст заметки с описанием какого-то важного дела или мысли.",
                createdAt = System.currentTimeMillis()
            ),
            onEditClick = {},
            onCopyClick = {},
            onDeleteClick = {}
        )
    }
}
