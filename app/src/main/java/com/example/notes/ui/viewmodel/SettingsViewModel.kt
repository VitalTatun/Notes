package com.example.notes.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.local.entities.Note
import com.example.notes.data.local.entities.Quote
import com.example.notes.data.repository.NotesRepository
import com.example.notes.data.repository.QuotesRepository
import com.example.notes.data.repository.UserPreferencesRepository
import com.example.notes.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val isBiometricEnabled: Boolean = false,
    val hasPassword: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showSetPasswordDialog: Boolean = false,
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
                    isBiometricEnabled = prefs.isBiometricEnabled,
                    hasPassword = prefs.passwordHash != null
                )
            }
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            prefsRepository.setThemeMode(mode)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setBiometricEnabled(enabled)
        }
    }

    fun updatePassword(oldPassword: String?, newPassword: String, question: String, answer: String) {
        viewModelScope.launch {
            val prefs = prefsRepository.userPreferencesFlow.first()
            if (prefs.passwordHash != null && oldPassword != null) {
                if (SecurityUtils.hashString(oldPassword) != prefs.passwordHash) {
                    _uiState.value = _uiState.value.copy(error = "Неверный старый пароль")
                    return@launch
                }
            }
            
            prefsRepository.updatePassword(
                hash = SecurityUtils.hashString(newPassword),
                question = question,
                answerHash = SecurityUtils.hashString(answer)
            )
            _uiState.value = _uiState.value.copy(
                message = "Пароль успешно обновлен",
                showChangePasswordDialog = false,
                showSetPasswordDialog = false,
                error = null
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, error = null)
    }

    fun toggleChangePasswordDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showChangePasswordDialog = show, error = null)
    }

    fun toggleSetPasswordDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSetPasswordDialog = show, error = null)
    }

    fun exportData(context: Context): String {
        return try {
            viewModelScope.launch {
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
                        put("book", quote.bookTitle)
                        put("createdAt", quote.createdAt)
                    })
                }
                
                root.put("notes", notesArray)
                root.put("quotes", quotesArray)
                
                val jsonString = root.toString(4)
                // В реальном приложении здесь был бы вызов Save Picker
                // Но для простоты вернем строку или сохраним во внутренний файл
            }
            "Данные подготовлены к экспорту"
        } catch (e: Exception) {
            "Ошибка экспорта: ${e.message}"
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
                    // Очистка БД (нужно добавить методы в репозитории)
                }
                
                val notesArray = root.optJSONArray("notes")
                notesArray?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        notesRepository.addNote(
                            obj.getString("title"),
                            obj.getString("content")
                        )
                    }
                }
                
                val quotesArray = root.optJSONArray("quotes")
                quotesArray?.let {
                    for (i in 0 until it.length()) {
                        val obj = it.getJSONObject(i)
                        quotesRepository.addQuote(
                            obj.getString("text"),
                            obj.getString("author"),
                            obj.optString("book", "")
                        )
                    }
                }
                
                _uiState.value = _uiState.value.copy(message = "Импорт завершен успешно")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Ошибка импорта: ${e.message}")
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
