package com.example.notes.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val bookTitle: String,
    val createdAt: Long = System.currentTimeMillis()
)
