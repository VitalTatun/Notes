package com.example.notes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode") // "LIGHT", "DARK", "SYSTEM"
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val USE_SYSTEM_FONT_SIZE = booleanPreferencesKey("use_system_font_size")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val BIOMETRIC_UNLOCK_ENABLED = booleanPreferencesKey("biometric_unlock_enabled")
        val PASSCODE_HASH = stringPreferencesKey("passcode_hash")
        val PASSCODE_SALT = stringPreferencesKey("passcode_salt")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            fontScale = preferences[PreferencesKeys.FONT_SCALE] ?: 1.0f,
            useSystemFontSize = preferences[PreferencesKeys.USE_SYSTEM_FONT_SIZE] ?: true,
            appLockEnabled = preferences[PreferencesKeys.APP_LOCK_ENABLED] ?: false,
            biometricUnlockEnabled = preferences[PreferencesKeys.BIOMETRIC_UNLOCK_ENABLED] ?: false,
            passcodeHash = preferences[PreferencesKeys.PASSCODE_HASH],
            passcodeSalt = preferences[PreferencesKeys.PASSCODE_SALT]
        )
    }

    suspend fun setFontScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.FONT_SCALE] = scale
        }
    }

    suspend fun setUseSystemFontSize(useSystem: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_SYSTEM_FONT_SIZE] = useSystem
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = enabled
        }
    }

    suspend fun setBiometricUnlockEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRIC_UNLOCK_ENABLED] = enabled
        }
    }

    suspend fun savePasscode(hash: String, salt: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSCODE_HASH] = hash
            preferences[PreferencesKeys.PASSCODE_SALT] = salt
        }
    }

    suspend fun clearPasscode() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.PASSCODE_HASH)
            preferences.remove(PreferencesKeys.PASSCODE_SALT)
            preferences[PreferencesKeys.APP_LOCK_ENABLED] = false
            preferences[PreferencesKeys.BIOMETRIC_UNLOCK_ENABLED] = false
        }
    }
}

data class UserPreferences(
    val themeMode: String,
    val fontScale: Float,
    val useSystemFontSize: Boolean,
    val appLockEnabled: Boolean,
    val biometricUnlockEnabled: Boolean,
    val passcodeHash: String?,
    val passcodeSalt: String?
)
