package com.example.notes.util

import java.text.SimpleDateFormat
import java.util.*

fun Long.formatDate(): String {
    val date = Date(this)
    val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return sdf.format(date)
}
