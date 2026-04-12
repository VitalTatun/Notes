package com.example.notes.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.viewmodel.NotesViewModel
import com.example.notes.ui.theme.SerifFontFamily
import com.example.notes.ui.viewmodel.QuotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsScreen(
    initialTab: Int = 0,
    notesViewModel: NotesViewModel,
    quotesViewModel: QuotesViewModel,
    onEditNote: (Note) -> Unit,
    onEditQuote: (Quote) -> Unit,
    onAddNote: () -> Unit,
    onAddQuote: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    val notesLazyListState = rememberLazyListState()
    val quotesLazyListState = rememberLazyListState()

    var isExpanded by rememberSaveable { mutableStateOf(true) }

    val fabNestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (consumed.y < -15f && isExpanded) {
                    isExpanded = false
                } else if (consumed.y > 15f && !isExpanded) {
                    isExpanded = true
                }
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    val isAtTop by remember {
        derivedStateOf {
            val state = if (selectedTab == 0) notesLazyListState else quotesLazyListState
            state.firstVisibleItemIndex == 0 && state.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isAtTop) {
        if (isAtTop) isExpanded = true
    }

    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var quoteToDelete by remember { mutableStateOf<Quote?>(null) }

    val focusManager = LocalFocusManager.current

    val selectedDate = if (selectedTab == 0) {
        notesViewModel.selectedDateMillis.collectAsState().value
    } else {
        quotesViewModel.selectedDateMillis.collectAsState().value
    }
    var showDatePicker by remember { mutableStateOf(false) }
    val bottomBarHeight = 80.dp
    val animatedBottomPadding by animateDpAsState(
        targetValue = if (isExpanded) bottomBarHeight else 0.dp,
        label = "bottomBarPadding"
    )

    val animatedFabOffsetY by animateDpAsState(
        targetValue = if (isExpanded) -bottomBarHeight - 16.dp else -16.dp,
        label = "fabOffsetY"
    )

    LaunchedEffect(selectedTab) {
        isExpanded = true
        focusManager.clearFocus()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        bottomBar = {
            // Оставляем пустым, чтобы управлять панелью вручную для плавной анимации
        },
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = if (selectedTab == 0) "Заметки" else "Цитаты",
                        style = LocalTextStyle.current.merge(
                            TextStyle(
                                fontFamily = SerifFontFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        ),
                        maxLines = 1
                    )
                },
                actions = {
                    if (selectedDate != null) {
                        IconButton(
                            onClick = {
                                focusManager.clearFocus()
                                if (selectedTab == 0) {
                                    notesViewModel.setSelectedDate(null)
                                } else {
                                    quotesViewModel.setSelectedDate(null)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Сбросить фильтр по дате")
                        }
                    }
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            showDatePicker = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Фильтр по дате",
                            tint = if (selectedDate != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                LocalContentColor.current
                            }
                        )
                    }
                    IconButton(
                        onClick = {
                            focusManager.clearFocus()
                            onSettingsClick()
                        }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Настройки")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.offset(y = animatedFabOffsetY),
                onClick = {
                    focusManager.clearFocus()
                    if (selectedTab == 0) onAddNote() else onAddQuote()
                },
                expanded = isExpanded,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { 
                    Text(
                        text = if (selectedTab == 0) "Заметка" else "Цитата",
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        softWrap = false
                    ) 
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = animatedBottomPadding)
                    .nestedScroll(fabNestedScrollConnection)
            ) {
                if (selectedTab == 0) {
                    val notes by notesViewModel.notes.collectAsState()
                    NotesScreen(
                        notes = notes,
                        onEditClick = onEditNote,
                        onDeleteConfirm = { noteToDelete = it },
                        lazyListState = notesLazyListState
                    )
                } else {
                    val quotes by quotesViewModel.quotes.collectAsState()
                    QuotesScreen(
                        quotes = quotes,
                        onEditClick = onEditQuote,
                        onDeleteConfirm = { quoteToDelete = it },
                        lazyListState = quotesLazyListState
                    )
                }
            }

            // Ручное управление NavigationBar для плавности
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = if (isExpanded) 0.dp else bottomBarHeight)
                    .alpha(if (isExpanded) 1f else 0f)
            ) {
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
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val pickedDate = datePickerState.selectedDateMillis
                        if (selectedTab == 0) {
                            notesViewModel.setSelectedDate(pickedDate)
                        } else {
                            quotesViewModel.setSelectedDate(pickedDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Выбрать")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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
