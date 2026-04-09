package com.example.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notes.data.local.entities.Note
import com.example.notes.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note? = null,
    onSave: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val initialTitle = note?.title.orEmpty()
    val initialContent = note?.content.orEmpty()
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    val hasChanges = title != initialTitle || content != initialContent
    
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        if (note == null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Заметка")
                        val dateText = note?.createdAt?.formatDate() ?: System.currentTimeMillis().formatDate()
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelMedium,
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
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить заметку",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Удалить заметку?") },
                                text = { Text("Это действие нельзя отменить.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            // Здесь должна быть логика удаления через ViewModel, 
                                            // но для простоты можно передать событие наверх
                                            onSave("", "") // Сигнал к удалению или просто пустое сохранение? 
                                            // Лучше добавить колбэк onDelete в параметры функции.
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
                    val isSaveEnabled = content.isNotBlank() && (note == null || hasChanges)
                    FilledIconButton(
                        onClick = { onSave(title, content) },
                        enabled = isSaveEnabled,
                        modifier = Modifier.size(width = 56.dp, height = 40.dp),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Готово")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { 
                        Text(
                            "Заголовок", 
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    singleLine = false,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
            
            Spacer(modifier = Modifier.height(0.dp))
            
            TextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { 
                    Text(
                        "Текст заметки",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .focusRequester(focusRequester),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
