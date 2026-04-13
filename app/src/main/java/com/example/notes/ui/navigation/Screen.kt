package com.example.notes.ui.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data class MainNotes(val tab: Int = 0) : Screen
    
    @Serializable
    data object QuoteSearch : Screen
    
    @Serializable
    data object Settings : Screen
    
    @Serializable
    data class QuoteDetail(val quoteId: Long = -1L) : Screen
    
    @Serializable
    data class NoteDetail(val noteId: Long = -1L) : Screen
}
