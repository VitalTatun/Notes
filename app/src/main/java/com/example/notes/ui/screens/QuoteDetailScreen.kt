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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.theme.NotesTheme
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quote: Quote? = null,
    onSave: (String, String) -> Unit,
    onDelete: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf(quote?.text ?: "") }
    var author by remember { mutableStateOf(quote?.author ?: "") }
    
    val canSave by remember(text, author, quote) {
        derivedStateOf {
            text.isNotBlank() && (quote == null || text != quote.text || author != quote.author)
        }
    }
    
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (quote == null) {
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
                    Text(text = if (quote == null) "Новая цитата" else "Цитата")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (quote != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить")
                        }
                    }

                    FilledIconButton(
                        onClick = { onSave(text, author) },
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
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            BasicTextField(
                value = author,
                onValueChange = { author = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .focusRequester(focusRequester),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (author.isEmpty()) {
                            Text(
                                "Автор",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box {
                        if (text.isEmpty()) {
                            Text(
                                "Текст цитаты",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить цитату?") },
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

@Preview(showBackground = true, name = "New Quote")
@Composable
fun QuoteDetailNewPreview() {
    NotesTheme {
        Surface {
            QuoteDetailScreen(
                quote = null,
                onSave = { _, _ -> },
                onBack = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit Quote")
@Composable
fun QuoteDetailEditPreview() {
    NotesTheme {
        Surface {
            QuoteDetailScreen(
                quote = Quote(
                    id = 1,
                    text = "Единственный способ делать великие дела — любить то, что вы делаете.",
                    author = "Стив Джобс",
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
fun QuoteDetailDarkPreview() {
    NotesTheme(themeMode = "DARK") {
        Surface {
            QuoteDetailScreen(
                quote = Quote(
                    id = 1,
                    text = "Будьте собой; все остальные роли уже заняты.",
                    author = "Оскар Уайльд",
                    createdAt = System.currentTimeMillis()
                ),
                onSave = { _, _ -> },
                onDelete = {},
                onBack = {}
            )
        }
    }
}
