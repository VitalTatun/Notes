package com.example.notes.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notes.data.local.entities.Note
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.util.formatDate
import androidx.compose.foundation.layout.size


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note? = null,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    
    // Оптимизация: вычисляем состояние кнопки сохранения только при изменении полей
    val canSave by remember(title, content, note) {
        derivedStateOf {
            content.isNotBlank() && (note == null || title != note.title || content != note.content)
        }
    }
    
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (note == null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (note == null) "Новая заметка" else "Заметка",
                        )
                        val dateText = note?.createdAt?.formatDate() ?: System.currentTimeMillis().formatDate()
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (note != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }

                    FilledIconButton(
                        onClick = { onSave(title, content) },
                        enabled = canSave,
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .height(32.dp)
                            .width(44.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Сохранить",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.systemBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding() // Автоматический отступ для клавиатуры
                .verticalScroll(scrollState)
        ) {
            TextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { 
                    Text(
                        "Заголовок", 
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ) 
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
            
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text(
                        "Начните писать...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить заметку?") },
            text = { Text("Это действие нельзя будет отменить.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete?.invoke()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "New Note")
@Composable
fun NoteDetailNewPreview() {
    NotesTheme {
        Surface {
            NoteDetailScreen(
                note = null,
                onSave = { _, _ -> },
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit Note")
@Composable
fun NoteDetailEditPreview() {
    NotesTheme {
        Surface {
            NoteDetailScreen(
                note = Note(
                    id = 1,
                    title = "Заголовок заметки",
                    content = "Это текст существующей заметки для редактирования.",
                    createdAt = System.currentTimeMillis()
                ),
                onSave = { _, _ -> },
                onDelete = {},
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun NoteDetailDarkPreview() {
    NotesTheme(themeMode = "DARK") {
        Surface {
            NoteDetailScreen(
                note = Note(
                    id = 1,
                    title = "Заголовок в темной теме",
                    content = "Проверка отображения экрана в темном режиме.",
                    createdAt = System.currentTimeMillis()
                ),
                onSave = { _, _ -> },
                onDelete = {},
                onBack = {}
            )
        }
    }
}
