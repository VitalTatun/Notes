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
        val THEME_MODE = stringPreferencesKey("theme_mode") // "LIGHT", "DARK", "SYSTEM"
        val IS_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val USE_SYSTEM_FONT_SIZE = booleanPreferencesKey("use_system_font_size")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        UserPreferences(
            themeMode = preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            isFirstLaunch = preferences[PreferencesKeys.IS_FIRST_LAUNCH] ?: true,
            fontScale = preferences[PreferencesKeys.FONT_SCALE] ?: 1.0f,
            useSystemFontSize = preferences[PreferencesKeys.USE_SYSTEM_FONT_SIZE] ?: true
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
    
    suspend fun setNotFirstLaunch() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_FIRST_LAUNCH] = false
        }
    }
}

data class UserPreferences(
    val themeMode: String,
    val isFirstLaunch: Boolean,
    val fontScale: Float,
    val useSystemFontSize: Boolean
)
