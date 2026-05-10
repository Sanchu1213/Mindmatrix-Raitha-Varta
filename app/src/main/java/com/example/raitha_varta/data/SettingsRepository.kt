package com.example.raitha_varta.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val PREF_CITY_KEY = stringPreferencesKey("pref_city")
    private val PREF_LANGUAGE_KEY = stringPreferencesKey("pref_lang")
    private val PREF_CROP_KEY = stringPreferencesKey("pref_crop")
    private val PREF_ALERTS_ENABLED_KEY = booleanPreferencesKey("pref_alerts_enabled")
    private val PREF_NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("pref_notifications_enabled")
    private val PREF_DARK_MODE_KEY = booleanPreferencesKey("pref_dark_mode")

    val preferredCity: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_CITY_KEY] ?: "Bangalore"
        }
    
    val isDarkMode: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_DARK_MODE_KEY]
        }

    val preferredLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_LANGUAGE_KEY] ?: "en" // Default to English
        }

    val preferredCrop: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_CROP_KEY] ?: "All"
        }

    val alertsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_ALERTS_ENABLED_KEY] ?: true
        }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PREF_NOTIFICATIONS_ENABLED_KEY] ?: true
        }

    suspend fun savePreferredCity(city: String) {
        context.dataStore.edit { preferences ->
            preferences[PREF_CITY_KEY] = city
        }
    }

    suspend fun savePreferredLanguage(langCode: String) {
        context.dataStore.edit { preferences ->
            preferences[PREF_LANGUAGE_KEY] = langCode
        }
    }

    suspend fun savePreferredCrop(crop: String) {
        context.dataStore.edit { preferences ->
            preferences[PREF_CROP_KEY] = crop
        }
    }

    suspend fun saveAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREF_ALERTS_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PREF_NOTIFICATIONS_ENABLED_KEY] = enabled
        }
    }

    suspend fun saveDarkMode(enabled: Boolean?) {
        context.dataStore.edit { preferences ->
            if (enabled == null) {
                preferences.remove(PREF_DARK_MODE_KEY)
            } else {
                preferences[PREF_DARK_MODE_KEY] = enabled
            }
        }
    }
}
