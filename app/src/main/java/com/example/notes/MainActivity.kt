package com.example.notes

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote
import com.example.notes.ui.screens.*
import com.example.notes.ui.theme.NotesTheme
import com.example.notes.ui.viewmodel.*

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NotesApplication
        
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(app.userPreferencesRepository)
            )
            val themeMode by mainViewModel.themeMode.collectAsState(initial = "SYSTEM")
            val fontScale by mainViewModel.fontScale.collectAsState(initial = 1.0f)
            val useSystemFontSize by mainViewModel.useSystemFontSize.collectAsState(initial = true)
            
            NotesTheme(
                themeMode = themeMode,
                fontScale = fontScale,
                useSystemFontSize = useSystemFontSize,
                dynamicColor = true // Возвращаем динамические цвета
            ) {
                val navController = rememberNavController()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "main_notes",
                        enterTransition = {
                            fadeIn(animationSpec = tween(220))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(180))
                        },
                        popEnterTransition = {
                            fadeIn(animationSpec = tween(220))
                        },
                        popExitTransition = {
                            fadeOut(animationSpec = tween(180))
                        }
                    ) {
                        composable(
                            route = "main_notes?tab={tab}",
                            arguments = listOf(
                                navArgument("tab") {
                                    type = NavType.IntType
                                    defaultValue = 0
                                }
                            )
                        ) { backStackEntry ->
                            val initialTab = backStackEntry.arguments?.getInt("tab") ?: 0
                            val notesViewModel: NotesViewModel = viewModel(
                                factory = NotesViewModel.Factory(app.notesRepository)
                            )
                            val quotesViewModel: QuotesViewModel = viewModel(
                                factory = QuotesViewModel.Factory(app.quotesRepository)
                            )
                            MainTabsScreen(
                                initialTab = initialTab,
                                notesViewModel = notesViewModel,
                                quotesViewModel = quotesViewModel,
                                onEditNote = { note ->
                                    navController.navigate("note_detail?noteId=${note.id}")
                                },
                                onEditQuote = { quote ->
                                    navController.navigate("quote_detail?quoteId=${quote.id}")
                                },
                                onAddNote = {
                                    navController.navigate("note_detail")
                                },
                                onAddQuote = {
                                    navController.navigate("quote_detail")
                                },
                                onSettingsClick = {
                                    navController.navigate("settings")
                                }
                            )
                        }
                        composable("settings") {
                            val settingsViewModel: SettingsViewModel = viewModel(
                                factory = SettingsViewModel.Factory(
                                    app.userPreferencesRepository,
                                    app.notesRepository,
                                    app.quotesRepository,
                                    initialThemeMode = themeMode,
                                    initialFontScale = fontScale,
                                    initialUseSystemFontSize = useSystemFontSize
                                )
                            )
                            SettingsScreen(
                                viewModel = settingsViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "quote_detail?quoteId={quoteId}",
                            arguments = listOf(
                                navArgument("quoteId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val quoteId = backStackEntry.arguments?.getLong("quoteId") ?: -1L
                            val quotesViewModel: QuotesViewModel = viewModel(
                                factory = QuotesViewModel.Factory(app.quotesRepository)
                            )
                            
                            var existingQuote by remember { mutableStateOf<Quote?>(null) }
                            var isQuoteLoading by remember(quoteId) { mutableStateOf(quoteId != -1L) }
                            
                            LaunchedEffect(quoteId) {
                                if (quoteId != -1L) {
                                    existingQuote = quotesViewModel.getQuoteById(quoteId)
                                }
                                isQuoteLoading = false
                            }
                            
                            when {
                                quoteId == -1L -> {
                                    QuoteDetailScreen(
                                        quote = null,
                                        onSave = { text, author ->
                                            quotesViewModel.addQuote(text, author)
                                            navController.popBackStack()
                                        },
                                        onDelete = null,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                isQuoteLoading -> {
                                    EditorLoadingScreen(
                                        title = "Цитата",
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                existingQuote != null -> {
                                    QuoteDetailScreen(
                                        quote = existingQuote,
                                        onSave = { text, author ->
                                            if (existingQuote != null) {
                                                quotesViewModel.updateQuote(existingQuote!!.copy(text = text, author = author))
                                            } else {
                                                quotesViewModel.addQuote(text, author)
                                            }
                                            navController.popBackStack()
                                        },
                                        onDelete = if (existingQuote != null) {
                                            {
                                                quotesViewModel.deleteQuote(existingQuote!!)
                                                navController.popBackStack()
                                            }
                                        } else {
                                            null
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                        composable(
                            route = "note_detail?noteId={noteId}",
                            arguments = listOf(
                                navArgument("noteId") {
                                    type = NavType.LongType
                                    defaultValue = -1L
                                }
                            )
                        ) { backStackEntry ->
                            val noteId = backStackEntry.arguments?.getLong("noteId") ?: -1L
                            val notesViewModel: NotesViewModel = viewModel(
                                factory = NotesViewModel.Factory(app.notesRepository)
                            )
                            
                            var existingNote by remember { mutableStateOf<Note?>(null) }
                            var isNoteLoading by remember(noteId) { mutableStateOf(noteId != -1L) }
                            
                            LaunchedEffect(noteId) {
                                if (noteId != -1L) {
                                    existingNote = notesViewModel.getNoteById(noteId)
                                }
                                isNoteLoading = false
                            }
                            
                            when {
                                noteId == -1L -> {
                                    NoteDetailScreen(
                                        note = null,
                                        onSave = { title, content ->
                                            notesViewModel.addNote(title, content)
                                            navController.popBackStack()
                                        },
                                        onDelete = null,
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                isNoteLoading -> {
                                    EditorLoadingScreen(
                                        title = "Заметка",
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                                existingNote != null -> {
                                    NoteDetailScreen(
                                        note = existingNote,
                                        onSave = { title, content ->
                                            if (existingNote != null) {
                                                notesViewModel.updateNote(existingNote!!.copy(title = title, content = content))
                                            } else {
                                                notesViewModel.addNote(title, content)
                                            }
                                            navController.popBackStack()
                                        },
                                        onDelete = if (existingNote != null) {
                                            {
                                                notesViewModel.deleteNote(existingNote!!)
                                                navController.popBackStack()
                                            }
                                        } else {
                                            null
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorLoadingScreen(
    title: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
