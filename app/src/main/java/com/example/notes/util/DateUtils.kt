package com.example.notes.util

import java.text.SimpleDateFormat
import java.util.*

private val dateFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
private val shortDateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
private val filterDateFormatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())

fun Long.formatDate(): String {
    return dateFormatter.format(Date(this))
}

fun Long.formatShortDate(): String {
    return shortDateFormatter.format(Date(this))
}

fun Long.formatFilterDate(): String {
    return filterDateFormatter.format(Date(this))
}

fun Long.isSameDay(other: Long): Boolean {
    val firstCalendar = Calendar.getInstance().apply { timeInMillis = this@isSameDay }
    val secondCalendar = Calendar.getInstance().apply { timeInMillis = other }
    return firstCalendar.get(Calendar.YEAR) == secondCalendar.get(Calendar.YEAR) &&
        firstCalendar.get(Calendar.DAY_OF_YEAR) == secondCalendar.get(Calendar.DAY_OF_YEAR)
}
