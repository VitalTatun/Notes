package com.example.notes.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor() {
    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private var hasInitialized = false

    fun syncWithSettings(appLockEnabled: Boolean, hasPasscode: Boolean) {
        if (!appLockEnabled || !hasPasscode) {
            _isLocked.value = false
            hasInitialized = true
            return
        }

        if (!hasInitialized) {
            _isLocked.value = true
            hasInitialized = true
        }
    }

    fun onAppBackgrounded(appLockEnabled: Boolean, hasPasscode: Boolean) {
        if (appLockEnabled && hasPasscode) {
            _isLocked.value = true
        }
    }

    fun unlock() {
        hasInitialized = true
        _isLocked.value = false
    }

    fun lockNow() {
        hasInitialized = true
        _isLocked.value = true
    }
}
