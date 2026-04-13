package com.example.notes.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.example.notes.ui.components.EditorLoadingScreen
import com.example.notes.ui.screens.*
import com.example.notes.ui.viewmodel.NotesViewModel
import com.example.notes.ui.viewmodel.QuotesViewModel
import com.example.notes.ui.viewmodel.SettingsViewModel

@Composable
fun NotesNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainNotes(),
        enterTransition = { fadeIn(animationSpec = tween(220)) },
        exitTransition = { fadeOut(animationSpec = tween(180)) },
        popEnterTransition = { fadeIn(animationSpec = tween(220)) },
        popExitTransition = { fadeOut(animationSpec = tween(180)) }
    ) {
        composable<Screen.MainNotes> { backStackEntry ->
            val route: Screen.MainNotes = backStackEntry.toRoute()
            val notesViewModel: NotesViewModel = hiltViewModel()
            val quotesViewModel: QuotesViewModel = hiltViewModel()
            
            MainTabsScreen(
                initialTab = route.tab,
                notesViewModel = notesViewModel,
                quotesViewModel = quotesViewModel,
                onEditNote = { note ->
                    navController.navigate(Screen.NoteDetail(note.id))
                },
                onEditQuote = { quote ->
                    navController.navigate(Screen.QuoteDetail(quote.id))
                },
                onAddNote = {
                    navController.navigate(Screen.NoteDetail())
                },
                onAddQuote = {
                    navController.navigate(Screen.QuoteDetail())
                },
                onSearchQuotesClick = {
                    navController.navigate(Screen.QuoteSearch)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings)
                }
            )
        }

        composable<Screen.QuoteSearch>(
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
        ) {
            val quotesViewModel: QuotesViewModel = hiltViewModel()
            val query by quotesViewModel.searchQuery.collectAsState()
            val quotes by quotesViewModel.quotes.collectAsState()

            QuoteSearchScreen(
                query = query,
                quotes = if (query.isBlank()) emptyList() else quotes,
                onQueryChange = quotesViewModel::onSearchQueryChange,
                onEditQuote = { quote ->
                    navController.navigate(Screen.QuoteDetail(quote.id))
                },
                onDeleteQuote = { quote ->
                    quotesViewModel.deleteQuote(quote)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Settings> {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = settingsViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable<Screen.QuoteDetail> {
            val quotesViewModel: QuotesViewModel = hiltViewModel()
            val existingQuote by quotesViewModel.existingQuote.collectAsState()
            
            val route: Screen.QuoteDetail = it.toRoute()
            val isNewQuote = route.quoteId == -1L

            if (isNewQuote) {
                QuoteDetailScreen(
                    quote = null,
                    onSave = { text, author ->
                        quotesViewModel.addQuote(text, author)
                        navController.popBackStack()
                    },
                    onDelete = null,
                    onBack = { navController.popBackStack() }
                )
            } else if (existingQuote == null) {
                EditorLoadingScreen(
                    title = "Цитата",
                    onBack = { navController.popBackStack() }
                )
            } else {
                QuoteDetailScreen(
                    quote = existingQuote,
                    onSave = { text, author ->
                        quotesViewModel.updateQuote(existingQuote!!.copy(text = text, author = author))
                        navController.popBackStack()
                    },
                    onDelete = {
                        quotesViewModel.deleteQuote(existingQuote!!)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable<Screen.NoteDetail> {
            val notesViewModel: NotesViewModel = hiltViewModel()
            val existingNote by notesViewModel.existingNote.collectAsState()
            
            val route: Screen.NoteDetail = it.toRoute()
            val isNewNote = route.noteId == -1L

            if (isNewNote) {
                NoteDetailScreen(
                    note = null,
                    onSave = { title, content ->
                        notesViewModel.addNote(title, content)
                        navController.popBackStack()
                    },
                    onDelete = null,
                    onBack = { navController.popBackStack() }
                )
            } else if (existingNote == null) {
                EditorLoadingScreen(
                    title = "Заметка",
                    onBack = { navController.popBackStack() }
                )
            } else {
                NoteDetailScreen(
                    note = existingNote,
                    onSave = { title, content ->
                        notesViewModel.updateNote(existingNote!!.copy(title = title, content = content))
                        navController.popBackStack()
                    },
                    onDelete = {
                        notesViewModel.deleteNote(existingNote!!)
                        navController.popBackStack()
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
