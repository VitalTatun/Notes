package com.example.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.notes.data.local.entities.Quote
import com.example.notes.util.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quote: Quote? = null,
    onSave: (String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var text by remember { mutableStateOf(quote?.text ?: "") }
    var author by remember { mutableStateOf(quote?.author ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (quote == null) "Новая цитата" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (text.isNotBlank() && author.isNotBlank()) {
                                onSave(text, author, "")
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Сохранить")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            TextField(
                value = author,
                onValueChange = { author = it },
                placeholder = { Text("Автор", style = MaterialTheme.typography.titleMedium) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                textStyle = MaterialTheme.typography.titleMedium
            )
            
            val dateText = quote?.createdAt?.formatDate() ?: System.currentTimeMillis().formatDate()
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Текст цитаты") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                )
            )
        }
    }
}
