package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    val themeMode: Flow<String> = repository.userPreferencesFlow.map { it.themeMode }
    val fontScale: Flow<Float> = repository.userPreferencesFlow.map { it.fontScale }
    val useSystemFontSize: Flow<Boolean> = repository.userPreferencesFlow.map { it.useSystemFontSize }

    init {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            if (prefs.isFirstLaunch) {
                _startDestination.value = "setup"
            } else if (prefs.passwordHash != null) {
                _startDestination.value = "login"
            } else {
                _startDestination.value = "main_notes"
            }
        }
    }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
