package com.example.notes.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.viewmodel.NotesViewModel
import com.example.notes.ui.viewmodel.QuotesViewModel

import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    initialTab: Int = 0,
    notesViewModel: NotesViewModel,
    quotesViewModel: QuotesViewModel,
    onNoteClick: (Note) -> Unit,
    onQuoteClick: (Quote) -> Unit,
    onAddNote: () -> Unit,
    onAddQuote: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var quoteToDelete by remember { mutableStateOf<Quote?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (selectedTab == 0) "Заметки" else "Цитаты") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
                    label = { Text("Заметки") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.FormatQuote, contentDescription = null) },
                    label = { Text("Цитаты") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (selectedTab == 0) onAddNote() else onAddQuote()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { innerPadding ->
        Surface(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                NotesScreen(
                    viewModel = notesViewModel,
                    onNoteClick = onNoteClick,
                    onDeleteConfirm = { noteToDelete = it }
                )
            } else {
                QuotesScreen(
                    viewModel = quotesViewModel,
                    onQuoteClick = onQuoteClick,
                    onDeleteConfirm = { quoteToDelete = it }
                )
            }
        }
    }

    // Диалог удаления заметки
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Удалить заметку?") },
            text = { Text("Это действие нельзя будет отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    notesViewModel.deleteNote(noteToDelete!!)
                    noteToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог удаления цитаты
    if (quoteToDelete != null) {
        AlertDialog(
            onDismissRequest = { quoteToDelete = null },
            title = { Text("Удалить цитату?") },
            text = { Text("Это действие нельзя будет отменить.") },
            confirmButton = {
                TextButton(onClick = {
                    quotesViewModel.deleteQuote(quoteToDelete!!)
                    quoteToDelete = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { quoteToDelete = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}
