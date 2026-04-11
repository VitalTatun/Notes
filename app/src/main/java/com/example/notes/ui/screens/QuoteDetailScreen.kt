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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notes.data.local.entities.Quote
import com.example.notes.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quote: Quote? = null,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    val initialText = quote?.text.orEmpty()
    val initialAuthor = quote?.author.orEmpty()
    var text by remember { mutableStateOf(quote?.text ?: "") }
    var author by remember { mutableStateOf(quote?.author ?: "") }
    val hasChanges = text != initialText || author != initialAuthor
    
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        if (quote == null) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(if (quote == null) "Новая цитата" else "Редактирование")
                        val dateText = quote?.createdAt?.formatDate() ?: System.currentTimeMillis().formatDate()
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
                    if (quote != null) {
                        var showDeleteDialog by remember { mutableStateOf(false) }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить цитату",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Удалить цитату?") },
                                text = { Text("Это действие нельзя отменить.") },
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
                    val isSaveEnabled = text.isNotBlank() && (quote == null || hasChanges)
                    FilledIconButton(
                        onClick = { onSave(text, author) },
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
                    value = author,
                    onValueChange = { author = it },
                    placeholder = {
                        Text(
                            "Автор",
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
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        "Текст цитаты",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
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
                textStyle = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
