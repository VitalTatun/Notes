package com.example.notes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as NotesApplication
        
        enableEdgeToEdge()
        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModel.Factory(app.userPreferencesRepository)
            )
            val startDestination by mainViewModel.startDestination.collectAsState()
            
            NotesTheme {
                if (startDestination != null) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = startDestination!!
                    ) {
                        composable("setup") {
                            val setupViewModel: SetupViewModel = viewModel(
                                factory = SetupViewModel.Factory(app.userPreferencesRepository)
                            )
                            SetupScreen(
                                viewModel = setupViewModel,
                                onFinish = {
                                    navController.navigate("main_notes") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("login") {
                            val loginViewModel: LoginViewModel = viewModel(
                                factory = LoginViewModel.Factory(app.userPreferencesRepository)
                            )
                            LoginScreen(
                                viewModel = loginViewModel,
                                onLoginSuccess = {
                                    navController.navigate("main_notes") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
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
                                onNoteClick = { note ->
                                    navController.navigate("note_detail?noteId=${note.id}")
                                },
                                onQuoteClick = { quote ->
                                    navController.navigate("quote_detail?quoteId=${quote.id}")
                                },
                                onAddNote = {
                                    navController.navigate("note_detail")
                                },
                                onAddQuote = {
                                    navController.navigate("quote_detail")
                                }
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
                            
                            LaunchedEffect(quoteId) {
                                if (quoteId != -1L) {
                                    existingQuote = quotesViewModel.getQuoteById(quoteId)
                                }
                            }
                            
                            if (quoteId == -1L || existingQuote != null) {
                                QuoteDetailScreen(
                                    quote = existingQuote,
                                    onSave = { text, author, book ->
                                        if (existingQuote != null) {
                                            quotesViewModel.updateQuote(existingQuote!!.copy(text = text, author = author, bookTitle = book))
                                        } else {
                                            quotesViewModel.addQuote(text, author, book)
                                        }
                                        navController.popBackStack()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
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
                            
                            LaunchedEffect(noteId) {
                                if (noteId != -1L) {
                                    existingNote = notesViewModel.getNoteById(noteId)
                                }
                            }
                            
                            if (noteId == -1L || existingNote != null) {
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
