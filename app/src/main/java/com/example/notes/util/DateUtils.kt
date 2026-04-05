package com.example.notes.util

import java.text.SimpleDateFormat
import java.util.*

private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

fun Long.formatDate(): String {
    return dateFormatter.format(Date(this))
}
