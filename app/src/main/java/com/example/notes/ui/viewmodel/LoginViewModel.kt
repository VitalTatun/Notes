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
    val isBiometricEnabled: Boolean = false,
    val canUseBiometric: Boolean = false,
    val securityQuestion: String? = null,
    val showRecoveryDialog: Boolean = false,
    val recoveryAnswer: String = "",
    val newPassword: String = "",
    val confirmNewPassword: String = "",
    val recoveryError: String? = null,
    val recoveryMessage: String? = null,
    val biometricMessage: String? = null
)

class LoginViewModel(private val repository: UserPreferencesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    isBiometricEnabled = prefs.isBiometricEnabled,
                    securityQuestion = prefs.securityQuestion
                )
            }
        }
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(isLoggedIn = true)
    }

    fun updateBiometricAvailability(canUseBiometric: Boolean) {
        _uiState.value = _uiState.value.copy(canUseBiometric = canUseBiometric)
    }

    fun onBiometricUnavailable(message: String) {
        _uiState.value = _uiState.value.copy(biometricMessage = message)
    }

    fun resetLoginState() {
        _uiState.value = _uiState.value.copy(isLoggedIn = false, password = "", error = null)
    }

    fun toggleRecoveryDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(
            showRecoveryDialog = show,
            recoveryAnswer = "",
            newPassword = "",
            confirmNewPassword = "",
            recoveryError = null,
            recoveryMessage = null
        )
    }

    fun onRecoveryAnswerChanged(answer: String) {
        _uiState.value = _uiState.value.copy(recoveryAnswer = answer, recoveryError = null)
    }

    fun onNewPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password, recoveryError = null)
    }

    fun onConfirmNewPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(confirmNewPassword = password, recoveryError = null)
    }

    fun clearRecoveryMessage() {
        _uiState.value = _uiState.value.copy(recoveryMessage = null, recoveryError = null)
    }

    fun clearBiometricMessage() {
        _uiState.value = _uiState.value.copy(biometricMessage = null)
    }

    fun login() {
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()

            if (SecurityUtils.verifyHash(_uiState.value.password, prefs.passwordHash)) {
                _uiState.value = _uiState.value.copy(isLoggedIn = true)
            } else {
                _uiState.value = _uiState.value.copy(error = "Неверный пароль")
            }
        }
    }

    fun recoverPassword() {
        val state = _uiState.value
        viewModelScope.launch {
            val prefs = repository.userPreferencesFlow.first()

            if (prefs.securityQuestion.isNullOrBlank() || prefs.securityAnswerHash.isNullOrBlank()) {
                _uiState.value = state.copy(recoveryError = "Восстановление недоступно")
                return@launch
            }
            if (!SecurityUtils.verifyHash(state.recoveryAnswer, prefs.securityAnswerHash)) {
                _uiState.value = state.copy(recoveryError = "Неверный ответ на контрольный вопрос")
                return@launch
            }
            if (state.newPassword.length < 4) {
                _uiState.value = state.copy(recoveryError = "Пароль слишком короткий (мин. 4 символа)")
                return@launch
            }
            if (state.newPassword != state.confirmNewPassword) {
                _uiState.value = state.copy(recoveryError = "Пароли не совпадают")
                return@launch
            }

            repository.updatePasswordHash(SecurityUtils.createHash(state.newPassword))
            _uiState.value = state.copy(
                password = "",
                showRecoveryDialog = false,
                recoveryAnswer = "",
                newPassword = "",
                confirmNewPassword = "",
                recoveryError = null,
                recoveryMessage = "Пароль успешно восстановлен"
            )
        }
    }

    class Factory(private val repository: UserPreferencesRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LoginViewModel(repository) as T
        }
    }
}
