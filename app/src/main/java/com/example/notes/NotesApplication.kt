package com.example.notes

import android.app.Application
import androidx.room.Room
import com.example.notes.data.local.AppDatabase
import com.example.notes.data.repository.NotesRepository
import com.example.notes.data.repository.QuotesRepository
import com.example.notes.data.repository.UserPreferencesRepository

class NotesApplication : Application() {
    
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "notes_database"
        ).build()
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(this)
    }

    val notesRepository: NotesRepository by lazy {
        NotesRepository(database.noteDao())
    }

    val quotesRepository: QuotesRepository by lazy {
        QuotesRepository(database.quoteDao())
    }
}
