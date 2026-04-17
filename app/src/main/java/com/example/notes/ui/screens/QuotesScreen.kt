package com.example.notes.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    quotes: List<Quote>,
    onEditClick: (Quote) -> Unit,
    onDeleteConfirm: (Quote) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
    emptyMessage: String = "Цитат пока нет",
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val clipboardManager = LocalClipboardManager.current
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (quotes.isEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = contentPadding
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxHeight(0.7f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emptyMessage, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = 16.dp, 
                    end = 16.dp, 
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(quotes, key = { it.id }) { quote ->
                    var showMenu by remember { mutableStateOf(false) }
                    
                    Column {
                        QuoteItem(
                            quote = quote,
                            onMoreClick = { showMenu = true }
                        )
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Редактировать") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEditClick(quote)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Скопировать") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    clipboardManager.setText(AnnotatedString(quote.text))
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteConfirm(quote)
                                }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuoteItem(
    quote: Quote,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quote.createdAt.formatDate(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            IconButton(
                onClick = onMoreClick, 
                modifier = Modifier.size(32.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.padding(6.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "“",
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color.Black
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 24.sp
                )

                if (quote.author.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "— ${quote.author}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuotesScreenPreview() {
    NotesTheme {
        QuotesScreen(
            quotes = listOf(
                Quote(1, "Первая цитата для примера в списке.", "Автор 1"),
                Quote(2, "Вторая цитата, которая немного длиннее, чтобы увидеть как работает перенос строк в карточке.", "Автор 2"),
                Quote(3, "Третья цитата.", "Автор 3")
            ),
            onEditClick = {},
            onDeleteConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuotesScreenEmptyPreview() {
    NotesTheme {
        QuotesScreen(
            quotes = emptyList(),
            onEditClick = {},
            onDeleteConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuoteItemPreview() {
    NotesTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            QuoteItem(
                quote = Quote(
                    id = 1,
                    text = "Это пример цитаты для превью. Она может быть довольно длинной, чтобы проверить перенос строк.",
                    author = "Автор Цитаты",
                    createdAt = System.currentTimeMillis()
                ),
                onMoreClick = {}
            )
        }
    }
}
