package com.example.notes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.notes.data.local.dao.NoteDao
import com.example.notes.data.local.dao.QuoteDao
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote

@Database(
    entities = [Note::class, Quote::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun quoteDao(): QuoteDao
}
