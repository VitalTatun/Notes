package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.UserPreferencesRepository
import com.example.notes.util.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun onConfirmPasswordChanged(confirm: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirm)
    }

    fun onQuestionChanged(question: String) {
        _uiState.value = _uiState.value.copy(securityQuestion = question)
    }

    fun onAnswerChanged(answer: String) {
        _uiState.value = _uiState.value.copy(securityAnswer = answer)
    }

    fun onBiometricToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isBiometricEnabled = enabled)
    }

    fun savePassword() {
        val state = _uiState.value
        if (state.password.length < 4) {
            _uiState.value = state.copy(error = "Пароль слишком короткий (мин. 4 символа)")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Пароли не совпадают")
            return
        }
        if (state.securityQuestion.isBlank() || state.securityAnswer.isBlank()) {
            _uiState.value = state.copy(error = "Заполните контрольный вопрос и ответ")
            return
        }

        viewModelScope.launch {
            repository.updatePassword(
                hash = SecurityUtils.createHash(state.password),
                question = state.securityQuestion,
                answerHash = SecurityUtils.createHash(state.securityAnswer)
            )
            repository.setBiometricEnabled(state.isBiometricEnabled)
            _uiState.value = state.copy(isSaved = true)
        }
    }
    
    fun skipSetup() {
        viewModelScope.launch {
            repository.setNotFirstLaunch()
            _uiState.value = _uiState.value.copy(isSkipped = true)
        }
    }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SetupViewModel(repository) as T
        }
    }
}

data class SetupUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = "",
    val isBiometricEnabled: Boolean = true,
    val error: String? = null,
    val isSaved: Boolean = false,
    val isSkipped: Boolean = false
)
