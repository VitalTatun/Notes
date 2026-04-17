package com.example.notes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.R
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.components.DateFilter
import com.example.notes.ui.components.DeleteDialog
import com.example.notes.ui.components.NotesBottomBar
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.ui.viewmodel.NotesViewModel
import com.example.notes.ui.viewmodel.QuotesViewModel
import com.example.notes.util.formatFilterDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    onSearchQuotesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialTab) { 2 }
    val isNotesTab = pagerState.currentPage == 0

    val notes by notesViewModel.notes.collectAsStateWithLifecycle()
    val quotes by quotesViewModel.quotes.collectAsStateWithLifecycle()
    val selectedNoteDate by notesViewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val selectedQuoteDate by quotesViewModel.selectedDateMillis.collectAsStateWithLifecycle()

    var showDatePicker by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var quoteToDelete by remember { mutableStateOf<Quote?>(null) }

    val currentSelectedDate = if (isNotesTab) selectedNoteDate else selectedQuoteDate

    MainTabsContent(
        pagerState = pagerState,
        isNotesTab = isNotesTab,
        notes = notes,
        quotes = quotes,
        currentSelectedDate = currentSelectedDate,
        onTabSelected = { tab -> scope.launch { pagerState.animateScrollToPage(tab) } },
        onAddClick = { if (isNotesTab) onAddNote() else onAddQuote() },
        onSearchQuotesClick = onSearchQuotesClick,
        onDateFilterClick = { showDatePicker = true },
        onEditNote = onEditNote,
        onEditQuote = onEditQuote,
        onDeleteNote = { noteToDelete = it },
        onDeleteQuote = { quoteToDelete = it },
        onSettingsClick = onSettingsClick
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentSelectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        if (isNotesTab) notesViewModel.setSelectedDate(null) else quotesViewModel.setSelectedDate(null)
                        showDatePicker = false
                    }) { Text("Сбросить") }
                    Row {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                        TextButton(onClick = {
                            val date = datePickerState.selectedDateMillis
                            if (isNotesTab) notesViewModel.setSelectedDate(date) else quotesViewModel.setSelectedDate(date)
                            showDatePicker = false
                        }) { Text(stringResource(R.string.ok)) }
                    }
                }
            }
        ) { DatePicker(state = datePickerState) }
    }

    noteToDelete?.let { note ->
        DeleteDialog(
            title = stringResource(R.string.delete_note_title),
            onConfirm = { notesViewModel.deleteNote(note); noteToDelete = null },
            onDismiss = { noteToDelete = null }
        )
    }

    quoteToDelete?.let { quote ->
        DeleteDialog(
            title = stringResource(R.string.delete_quote_title),
            onConfirm = { quotesViewModel.deleteQuote(quote); quoteToDelete = null },
            onDismiss = { quoteToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabsContent(
    pagerState: PagerState,
    isNotesTab: Boolean,
    notes: List<Note>,
    quotes: List<Quote>,
    currentSelectedDate: Long?,
    onTabSelected: (Int) -> Unit,
    onAddClick: () -> Unit,
    onSearchQuotesClick: () -> Unit,
    onDateFilterClick: () -> Unit,
    onEditNote: (Note) -> Unit,
    onEditQuote: (Quote) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onDeleteQuote: (Quote) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val notesLazyListState = rememberLazyListState()
    val quotesLazyListState = rememberLazyListState()

    var isUIExpanded by rememberSaveable { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val currentLazyListState = remember(isNotesTab) {
        if (isNotesTab) notesLazyListState else quotesLazyListState
    }

    val isAtTop by remember {
        derivedStateOf {
            currentLazyListState.firstVisibleItemIndex == 0 && 
            currentLazyListState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isAtTop) { if (isAtTop) isUIExpanded = true }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (consumed.y < -15f && isUIExpanded) isUIExpanded = false
                else if (consumed.y > 15f && !isUIExpanded) isUIExpanded = true
                return super.onPostScroll(consumed, available, source)
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Text(
                        text = stringResource(if (isNotesTab) R.string.notes else R.string.quotes),
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                actions = {
                    DateFilter(
                        isSelected = currentSelectedDate != null,
                        dateText = currentSelectedDate?.formatFilterDate() ?: "",
                        onClick = onDateFilterClick
                    )
                    IconButton(onClick = onSearchQuotesClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.search)
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NotesBottomBar(
                selectedTab = pagerState.currentPage,
                onTabSelected = onTabSelected,
                isExpanded = isUIExpanded
            )
        },
        floatingActionButton = {
            val haptic = LocalHapticFeedback.current
            FloatingActionButton(
                onClick = { 
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onAddClick() 
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add, 
                    contentDescription = stringResource(if (isNotesTab) R.string.add_note else R.string.add_quote)
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) { page ->
            when (page) {
                0 -> NotesScreen(
                    notes = notes,
                    onEditClick = onEditNote,
                    onDeleteConfirm = onDeleteNote,
                    lazyListState = notesLazyListState,
                    contentPadding = innerPadding
                )
                1 -> QuotesScreen(
                    quotes = quotes,
                    onEditClick = onEditQuote,
                    onDeleteConfirm = onDeleteQuote,
                    lazyListState = quotesLazyListState,
                    contentPadding = innerPadding
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainTabsScreenPreview() {
    NotesTheme {
        val pagerState = rememberPagerState { 2 }
        MainTabsContent(
            pagerState = pagerState,
            isNotesTab = pagerState.currentPage == 0,
            notes = listOf(
                Note(id = 1, title = "Заметка 1", content = "Текст первой заметки", createdAt = System.currentTimeMillis()),
                Note(id = 2, title = "Заметка 2", content = "Текст второй заметки", createdAt = System.currentTimeMillis())
            ),
            quotes = listOf(
                Quote(id = 1, text = "Первая цитата", author = "Автор 1", createdAt = System.currentTimeMillis()),
                Quote(id = 2, text = "Вторая цитата", author = "Автор 2", createdAt = System.currentTimeMillis())
            ),
            currentSelectedDate = null,
            onTabSelected = {},
            onAddClick = {},
            onSearchQuotesClick = {},
            onDateFilterClick = {},
            onEditNote = {},
            onEditQuote = {},
            onDeleteNote = {},
            onDeleteQuote = {},
            onSettingsClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainTabsScreenFilteredPreview() {
    NotesTheme {
        val pagerState = rememberPagerState { 2 }
        MainTabsContent(
            pagerState = pagerState,
            isNotesTab = pagerState.currentPage == 0,
            notes = listOf(
                Note(id = 1, title = "Заметка 1", content = "Текст первой заметки", createdAt = System.currentTimeMillis())
            ),
            quotes = emptyList(),
            currentSelectedDate = System.currentTimeMillis(),
            onTabSelected = {},
            onAddClick = {},
            onSearchQuotesClick = {},
            onDateFilterClick = {},
            onEditNote = {},
            onEditQuote = {},
            onDeleteNote = {},
            onDeleteQuote = {},
            onSettingsClick = {}
        )
    }
}
