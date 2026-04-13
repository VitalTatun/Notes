package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.notes.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val repository: UserPreferencesRepository) : ViewModel() {

    val themeMode: Flow<String> = repository.userPreferencesFlow.map { it.themeMode }
    val fontScale: Flow<Float> = repository.userPreferencesFlow.map { it.fontScale }
    val useSystemFontSize: Flow<Boolean> = repository.userPreferencesFlow.map { it.useSystemFontSize }
}
