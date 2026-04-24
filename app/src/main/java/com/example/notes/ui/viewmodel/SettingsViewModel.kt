package com.example.notes.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.NotesRepository
import com.example.notes.data.repository.QuotesRepository
import com.example.notes.data.repository.UserPreferencesRepository
import com.example.notes.security.AppLockManager
import com.example.notes.security.PasscodeSecurityManager
import com.example.notes.util.ShareUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val fontScale: Float = 1.0f,
    val useSystemFontSize: Boolean = true,
    val appLockEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
    val hasPasscode: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefsRepository: UserPreferencesRepository,
    private val notesRepository: NotesRepository,
    private val quotesRepository: QuotesRepository,
    private val passcodeSecurityManager: PasscodeSecurityManager,
    private val appLockManager: AppLockManager
) : ViewModel() {

    private val preferencesState = prefsRepository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val _uiState = MutableStateFlow(SettingsUiState(isLoading = true))
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesState.filterNotNull().collect { prefs ->
                _uiState.update {
                    it.copy(
                        themeMode = prefs.themeMode,
                        fontScale = prefs.fontScale,
                        useSystemFontSize = prefs.useSystemFontSize,
                        appLockEnabled = prefs.appLockEnabled,
                        biometricUnlockEnabled = prefs.biometricUnlockEnabled,
                        hasPasscode = !prefs.passcodeHash.isNullOrBlank() && !prefs.passcodeSalt.isNullOrBlank(),
                        isLoading = false
                    )
                }
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

    fun setupAppLock(passcode: String, enableBiometric: Boolean, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val normalized = passcode.trim()
            if (normalized.length < MIN_PASSCODE_LENGTH) {
                _uiState.update { it.copy(error = "Пароль должен быть не короче 4 символов") }
                onResult(false)
                return@launch
            }

            val (hash, salt) = passcodeSecurityManager.createHash(normalized)
            prefsRepository.savePasscode(hash, salt)
            prefsRepository.setAppLockEnabled(true)
            prefsRepository.setBiometricUnlockEnabled(enableBiometric)
            appLockManager.unlock()
            _uiState.update { it.copy(message = "Защита приложения включена") }
            onResult(true)
        }
    }

    fun changePasscode(currentPasscode: String, newPasscode: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (!verifyCurrentPasscode(currentPasscode)) {
                _uiState.update { it.copy(error = "Текущий пароль введён неверно") }
                onResult(false)
                return@launch
            }

            val normalizedNew = newPasscode.trim()
            if (normalizedNew.length < MIN_PASSCODE_LENGTH) {
                _uiState.update { it.copy(error = "Новый пароль должен быть не короче 4 символов") }
                onResult(false)
                return@launch
            }

            val (hash, salt) = passcodeSecurityManager.createHash(normalizedNew)
            prefsRepository.savePasscode(hash, salt)
            _uiState.update { it.copy(message = "Пароль приложения изменён") }
            onResult(true)
        }
    }

    fun disableAppLock(currentPasscode: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            if (!verifyCurrentPasscode(currentPasscode)) {
                _uiState.update { it.copy(error = "Текущий пароль введён неверно") }
                onResult(false)
                return@launch
            }

            prefsRepository.clearPasscode()
            appLockManager.unlock()
            _uiState.update { it.copy(message = "Защита приложения отключена") }
            onResult(true)
        }
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!_uiState.value.hasPasscode) {
                _uiState.update { it.copy(error = "Сначала задайте пароль приложения") }
                return@launch
            }

            prefsRepository.setBiometricUnlockEnabled(enabled)
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null, error = null) }
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
                _uiState.update { it.copy(message = "Данные подготовлены к экспорту") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка экспорта: ${e.message}") }
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch {
            try {
                notesRepository.deleteAllNotes()
                quotesRepository.deleteAllQuotes()
                _uiState.update { it.copy(message = "Все записи удалены") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка при удалении: ${e.message}") }
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
                            content = obj.optString("content", obj.optString("title")),
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

                _uiState.update { it.copy(message = "Импорт завершен успешно") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка импорта: ${e.message}") }
            }
        }
    }

    fun addSampleData() {
        viewModelScope.launch {
            try {
                val now = System.currentTimeMillis()
                val dayMillis = TimeUnit.DAYS.toMillis(1)

                for (i in 1..50) {
                    val daysAgo = (i - 1) % 7
                    val createdAt = now - (daysAgo * dayMillis) - (i * 60_000L)
                    notesRepository.addNote(
                        content = "Это содержание тестовой заметки номер $i. Здесь может быть довольно длинный текст для проверки прокрутки и производительности списка в Material 3.",
                        createdAt = createdAt
                    )
                }

                val authors = listOf("Марк Твен", "Альберт Эйнштейн", "Стив Джобс", "Оскар Уайльд", "Лев Толстой")
                val quotes = listOf(
                    "Единственный способ делать великие дела — любить то, что вы делаете.",
                    "Воображение важнее, чем знания.",
                    "Будь собой, все остальные роли уже заняты.",
                    "Все счастливые семьи похожи друг на друга, каждая несчастливая семья несчастлива по-своему.",
                    "Слухи о моей смерти несколько преувеличены."
                )

                for (i in 1..50) {
                    val daysAgo = (i - 1) % 7
                    val createdAt = now - (daysAgo * dayMillis) - (i * 90_000L)
                    quotesRepository.addQuote(
                        text = quotes[i % quotes.size] + " (Вариант #$i)",
                        author = authors[i % authors.size],
                        createdAt = createdAt
                    )
                }
                _uiState.update { it.copy(message = "Добавлено по 50 тестовых записей за последние 7 дней") }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Ошибка при добавлении: ${e.message}") }
            }
        }
    }

    private fun verifyCurrentPasscode(rawPasscode: String): Boolean {
        val preferences = preferencesState.value ?: return false
        val storedHash = preferences.passcodeHash ?: return false
        val storedSalt = preferences.passcodeSalt ?: return false
        return passcodeSecurityManager.verify(rawPasscode.trim(), storedHash, storedSalt)
    }

    private companion object {
        const val MIN_PASSCODE_LENGTH = 4
    }
}
