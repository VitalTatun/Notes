package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        checkStartDestination()
    }

    private fun checkStartDestination() {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            _startDestination.value = when {
                prefs.isFirstLaunch -> "setup"
                prefs.passwordHash != null -> "login"
                else -> "main_notes"
            }
        }
    }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
