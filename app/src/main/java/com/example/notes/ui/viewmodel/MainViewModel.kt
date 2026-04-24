package com.example.notes.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notes.data.repository.UserPreferencesRepository
import com.example.notes.security.AppLockManager
import com.example.notes.security.PasscodeSecurityManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val isReady: Boolean = false,
    val themeMode: String = "SYSTEM",
    val fontScale: Float = 1.0f,
    val useSystemFontSize: Boolean = true,
    val appLockEnabled: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
    val hasPasscode: Boolean = false,
    val isLocked: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: UserPreferencesRepository,
    private val appLockManager: AppLockManager,
    private val passcodeSecurityManager: PasscodeSecurityManager
) : ViewModel() {

    private val preferencesState = repository.userPreferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )

    private val preferencesFlow = preferencesState.filterNotNull().map { preferences ->
        val hasPasscode = !preferences.passcodeHash.isNullOrBlank() && !preferences.passcodeSalt.isNullOrBlank()
        appLockManager.syncWithSettings(
            appLockEnabled = preferences.appLockEnabled,
            hasPasscode = hasPasscode
        )

        MainUiState(
            isReady = true,
            themeMode = preferences.themeMode,
            fontScale = preferences.fontScale,
            useSystemFontSize = preferences.useSystemFontSize,
            appLockEnabled = preferences.appLockEnabled,
            biometricUnlockEnabled = preferences.biometricUnlockEnabled,
            hasPasscode = hasPasscode
        )
    }

    val uiState: StateFlow<MainUiState> = combine(
        preferencesFlow,
        appLockManager.isLocked
    ) { uiState, isLocked ->
        uiState.copy(isLocked = uiState.appLockEnabled && uiState.hasPasscode && isLocked)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState()
    )

    fun onAppBackgrounded() {
        val current = uiState.value
        appLockManager.onAppBackgrounded(
            appLockEnabled = current.appLockEnabled,
            hasPasscode = current.hasPasscode
        )
    }

    fun unlockWithPasscode(input: String): Boolean {
        val preferences = preferencesState.value ?: return false
        val hash = preferences.passcodeHash ?: return false
        val salt = preferences.passcodeSalt ?: return false
        val isValid = passcodeSecurityManager.verify(input, hash, salt)
        if (isValid) {
            appLockManager.unlock()
        }
        return isValid
    }

    fun unlockWithBiometricSuccess() {
        appLockManager.unlock()
    }
}
