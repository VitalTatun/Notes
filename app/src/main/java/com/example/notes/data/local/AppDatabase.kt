package com.example.notes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.notes.data.local.dao.NoteDao
import com.example.notes.data.local.dao.QuoteDao
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote

@Database(
    entities = [Note::class, Quote::class],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS quotes_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        text TEXT NOT NULL,
                        author TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    INSERT INTO quotes_new (id, text, author, createdAt)
                    SELECT id, text, author, createdAt FROM quotes
                    """.trimIndent()
                )

                database.execSQL("DROP TABLE quotes")
                database.execSQL("ALTER TABLE quotes_new RENAME TO quotes")
            }
        }
    }
}
