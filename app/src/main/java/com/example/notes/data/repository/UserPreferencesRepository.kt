package com.example.notes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val PASSWORD_HASH = stringPreferencesKey("password_hash")
        val SECURITY_QUESTION = stringPreferencesKey("security_question")
        val SECURITY_ANSWER_HASH = stringPreferencesKey("security_answer_hash")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode") // "LIGHT", "DARK", "SYSTEM"
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            passwordHash = preferences[PreferencesKeys.PASSWORD_HASH],
            securityQuestion = preferences[PreferencesKeys.SECURITY_QUESTION],
            securityAnswerHash = preferences[PreferencesKeys.SECURITY_ANSWER_HASH],
            isBiometricEnabled = preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] ?: false,
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true
        )
    }

    suspend fun updatePassword(hash: String, question: String, answerHash: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PASSWORD_HASH] = hash
            preferences[PreferencesKeys.SECURITY_QUESTION] = question
            preferences[PreferencesKeys.SECURITY_ANSWER_HASH] = answerHash
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode
        }
    }
    
    suspend fun setNotFirstLaunch() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }
}

data class UserPreferences(
    val passwordHash: String?,
    val securityQuestion: String?,
    val securityAnswerHash: String?,
    val isBiometricEnabled: Boolean,
    val themeMode: String,
    val isFirstLaunch: Boolean
)
