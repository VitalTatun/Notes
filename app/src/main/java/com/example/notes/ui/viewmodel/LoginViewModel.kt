package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.UserPreferencesRepository
import com.example.notes.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class LoginUiState(
    val password: String = "",
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isBiometricEnabled: Boolean = false
)

class LoginViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(isBiometricEnabled = prefs.isBiometricEnabled)
            }
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(isLoggedIn = true)
    }

    fun login() {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()
            val inputHash = SecurityUtils.hashString(_uiState.value.password)
            
            if (inputHash == prefs.passwordHash) {
                _uiState.value = _uiState.value.copy(isLoggedIn = true)
            } else {
                _uiState.value = _uiState.value.copy(error = "Неверный пароль")
            }
        }
    }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repository) as T
        }
    }
}
