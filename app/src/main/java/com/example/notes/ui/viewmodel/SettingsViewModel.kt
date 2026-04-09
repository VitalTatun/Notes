package com.example.notes.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.NotesRepository
import com.example.notes.data.repository.QuotesRepository
import com.example.notes.data.repository.UserPreferencesRepository
import com.example.notes.util.ShareUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val fontScale: Float = 1.0f,
    val useSystemFontSize: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

class SettingsViewModel(
    private val prefsRepository: UserPreferencesRepository,
    private val notesRepository: NotesRepository,
    private val quotesRepository: QuotesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefsRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    themeMode = prefs.themeMode,
                    fontScale = prefs.fontScale,
                    useSystemFontSize = prefs.useSystemFontSize
                )
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefsRepository.setThemeMode(mode)
        }
    }

    fun setFontScale(scale: Float) {
        viewModelScope.launch {
            prefsRepository.setFontScale(scale)
        }
    }

    fun setUseSystemFontSize(useSystem: Boolean) {
        viewModelScope.launch {
            prefsRepository.setUseSystemFontSize(useSystem)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun exportData(context: Context) {
        viewModelScope.launch {
            try {
                val notes = notesRepository.allNotes.first()
                val quotes = quotesRepository.allQuotes.first()
                
                val root = JSONObject()
                val notesArray = JSONArray()
                notes.forEach { note ->
                    notesArray.put(JSONObject().apply {
                        put("title", note.title)
                        put("content", note.content)
                        put("createdAt", note.createdAt)
                    })
                }
                
                val quotesArray = JSONArray()
                quotes.forEach { quote ->
                    quotesArray.put(JSONObject().apply {
                        put("text", quote.text)
                        put("author", quote.author)
                        put("createdAt", quote.createdAt)
                    })
                }
                
                root.put("notes", notesArray)
                root.put("quotes", quotesArray)
                
                val jsonString = root.toString(4)
                val fileName = "notes_export_${System.currentTimeMillis()}.json"
                
                ShareUtils.shareJsonFile(context, jsonString, fileName)
                _uiState.value = _uiState.value.copy(message = "Данные подготовлены к экспорту")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка экспорта: ${e.message}")
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            try {
                notesRepository.deleteAllNotes()
                quotesRepository.deleteAllQuotes()
                _uiState.value = _uiState.value.copy(message = "Все записи удалены")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка при удалении: ${e.message}")
            }
        }
    }

    fun importData(context: Context, uri: Uri, replace: Boolean) {
        viewModelScope.launch {
            try {
                val contentResolver = context.contentResolver
                val stringBuilder = StringBuilder()
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stringBuilder.append(line)
                        }
                    }
                }
                
                val root = JSONObject(stringBuilder.toString())
                
                if (replace) {
                    notesRepository.deleteAllNotes()
                    quotesRepository.deleteAllQuotes()
                }
                
                val notesArray = root.optJSONArray("notes")
                notesArray?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        notesRepository.addNote(
                            title = obj.getString("title"),
                            content = obj.getString("content"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    }
                }
                
                val quotesArray = root.optJSONArray("quotes")
                quotesArray?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        quotesRepository.addQuote(
                            text = obj.getString("text"),
                            author = obj.getString("author"),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    }
                }
                
                _uiState.value = _uiState.value.copy(message = "Импорт завершен успешно")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка импорта: ${e.message}")
            }
        }
    }

    fun addSampleData() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val dayMillis = TimeUnit.DAYS.toMillis(1)

                // Добавляем 50 заметок
                for (i in 1..50) {
                    val daysAgo = (i - 1) % 7
                    val createdAt = now - (daysAgo * dayMillis) - (i * 60_000L)
                    notesRepository.addNote(
                        title = "Заметка #$i",
                        content = "Это содержание тестовой заметки номер $i. Здесь может быть довольно длинный текст для проверки прокрутки и производительности списка в Material 3.",
                        createdAt = createdAt
                    )
                }
                
                // Список авторов и цитат для разнообразия
                val authors = listOf("Марк Твен", "Альберт Эйнштейн", "Стив Джобс", "Оскар Уайльд", "Лев Толстой")
                val quotes = listOf(
                    "Единственный способ делать великие дела — любить то, что вы делаете.",
                    "Воображение важнее, чем знания.",
                    "Будь собой, все остальные роли уже заняты.",
                    "Все счастливые семьи похожи друг на друга, каждая несчастливая семья несчастлива по-своему.",
                    "Слухи о моей смерти несколько преувеличены."
                )

                // Добавляем 50 цитат
                for (i in 1..50) {
                    val daysAgo = (i - 1) % 7
                    val createdAt = now - (daysAgo * dayMillis) - (i * 90_000L)
                    quotesRepository.addQuote(
                        text = quotes[i % quotes.size] + " (Вариант #$i)",
                        author = authors[i % authors.size],
                        createdAt = createdAt
                    )
                }
                _uiState.value = _uiState.value.copy(message = "Добавлено по 50 тестовых записей за последние 7 дней")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка при добавлении: ${e.message}")
            }
        }
    }

    class Factory(
        private val prefsRepository: UserPreferencesRepository,
        private val notesRepository: NotesRepository,
        private val quotesRepository: QuotesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(prefsRepository, notesRepository, quotesRepository) as T
        }
    }
}
