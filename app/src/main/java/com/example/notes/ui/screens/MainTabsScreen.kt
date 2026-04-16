package com.example.notes.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notes.R
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.components.DeleteDialog
import com.example.notes.ui.components.NotesBottomBar
import com.example.notes.ui.viewmodel.NotesViewModel
import com.example.notes.ui.viewmodel.QuotesViewModel
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
    val focusManager = LocalFocusManager.current
    val pagerState = rememberPagerState(initialPage = initialTab) { 2 }
    val isNotesTab = pagerState.currentPage == 0

    val notesLazyListState = rememberLazyListState()
    val quotesLazyListState = rememberLazyListState()

    val notes by notesViewModel.notes.collectAsStateWithLifecycle()
    val quotes by quotesViewModel.quotes.collectAsStateWithLifecycle()
    val selectedNoteDate by notesViewModel.selectedDateMillis.collectAsStateWithLifecycle()
    val selectedQuoteDate by quotesViewModel.selectedDateMillis.collectAsStateWithLifecycle()

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

    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var quoteToDelete by remember { mutableStateOf<Quote?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val currentSelectedDate = if (isNotesTab) selectedNoteDate else selectedQuoteDate

    val animatedFabOffsetY by animateDpAsState(
        targetValue = if (isUIExpanded) (-80).dp else 0.dp,
        label = "fabOffsetY"
    )

    val animatedBottomPadding by animateDpAsState(
        targetValue = if (isUIExpanded) 80.dp else 0.dp,
        label = "bottomPadding"
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = { Text(stringResource(if (isNotesTab) R.string.notes else R.string.quotes)) },
                actions = {
                    if (!isNotesTab) {
                        IconButton(onClick = onSearchQuotesClick) {
                            Icon(Icons.Default.Search, stringResource(R.string.search))
                        }
                    }
                    if (currentSelectedDate != null) {
                        IconButton(onClick = {
                            if (isNotesTab) notesViewModel.setSelectedDate(null)
                            else quotesViewModel.setSelectedDate(null)
                        }) {
                            Icon(Icons.Default.Close, stringResource(R.string.reset_date))
                        }
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.date),
                            tint = if (currentSelectedDate != null) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, stringResource(R.string.settings))
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.offset(y = animatedFabOffsetY),
                onClick = { if (isNotesTab) onAddNote() else onAddQuote() }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(if (isNotesTab) R.string.add_note else R.string.add_quote))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = animatedBottomPadding)
                    .nestedScroll(nestedScrollConnection)
            ) { page ->
                when (page) {
                    0 -> NotesScreen(
                        notes = notes,
                        onEditClick = onEditNote,
                        onDeleteConfirm = { noteToDelete = it },
                        lazyListState = notesLazyListState
                    )
                    1 -> QuotesScreen(
                        quotes = quotes,
                        onEditClick = onEditQuote,
                        onDeleteConfirm = { quoteToDelete = it },
                        lazyListState = quotesLazyListState
                    )
                }
            }

            NotesBottomBar(
                selectedTab = pagerState.currentPage,
                onTabSelected = { tab -> scope.launch { pagerState.animateScrollToPage(tab) } },
                isExpanded = isUIExpanded,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = currentSelectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis
                    if (isNotesTab) notesViewModel.setSelectedDate(date) else quotesViewModel.setSelectedDate(date)
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.cancel)) } }
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
