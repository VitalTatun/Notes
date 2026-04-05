package com.example.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
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
    onAddQuote: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var quoteToDelete by remember { mutableStateOf<Quote?>(null) }
    
    var noteToOptions by remember { mutableStateOf<Note?>(null) }
    var quoteToOptions by remember { mutableStateOf<Quote?>(null) }
    
    val clipboardManager = LocalClipboardManager.current
    
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    
    val notesSearchQuery by notesViewModel.searchQuery.collectAsState()
    val quotesSearchQuery by quotesViewModel.searchQuery.collectAsState()

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = if (selectedTab == 0) notesSearchQuery else quotesSearchQuery,
                            onValueChange = {
                                if (selectedTab == 0) notesViewModel.onSearchQueryChange(it)
                                else quotesViewModel.onSearchQueryChange(it)
                            },
                            placeholder = { Text("Поиск...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            textStyle = TextStyle(fontSize = 18.sp),
                            singleLine = true
                        )
                    } else {
                        Text(if (selectedTab == 0) "Заметки" else "Цитаты")
                    }
                },
                actions = {
                    if (isSearchActive) {
                        IconButton(onClick = { 
                            isSearchActive = false
                            notesViewModel.onSearchQueryChange("")
                            quotesViewModel.onSearchQueryChange("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть поиск")
                        }
                    } else {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Настройки")
                        }
                    }
                }
            )
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
                    onDeleteConfirm = { noteToOptions = it }
                )
            } else {
                QuotesScreen(
                    viewModel = quotesViewModel,
                    onQuoteClick = onQuoteClick,
                    onDeleteConfirm = { quoteToOptions = it }
                )
            }
        }
    }

    // Диалог действий для заметки
    if (noteToOptions != null) {
        AlertDialog(
            onDismissRequest = { noteToOptions = null },
            title = { Text("Выберите действие") },
            confirmButton = {
                val textToCopy = if (noteToOptions!!.title.isNotBlank()) {
                    "${noteToOptions!!.title}\n\n${noteToOptions!!.content}"
                } else {
                    noteToOptions!!.content
                }
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(textToCopy))
                    noteToOptions = null
                }) {
                    Text("Копировать")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    noteToDelete = noteToOptions
                    noteToOptions = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }

    // Диалог действий для цитаты
    if (quoteToOptions != null) {
        AlertDialog(
            onDismissRequest = { quoteToOptions = null },
            title = { Text("Выберите действие") },
            confirmButton = {
                val textToCopy = "\"${quoteToOptions!!.text}\"\n— ${quoteToOptions!!.author}${if (quoteToOptions!!.bookTitle.isNotBlank()) " (${quoteToOptions!!.bookTitle})" else ""}"
                TextButton(onClick = {
                    clipboardManager.setText(AnnotatedString(textToCopy))
                    quoteToOptions = null
                }) {
                    Text("Копировать")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    quoteToDelete = quoteToOptions
                    quoteToOptions = null
                }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            }
        )
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
