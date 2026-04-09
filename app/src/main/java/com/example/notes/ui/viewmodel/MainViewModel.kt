package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notes.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    val themeMode: Flow<String> = repository.userPreferencesFlow.map { it.themeMode }
    val fontScale: Flow<Float> = repository.userPreferencesFlow.map { it.fontScale }
    val useSystemFontSize: Flow<Boolean> = repository.userPreferencesFlow.map { it.useSystemFontSize }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository) as T
        }
    }
}
