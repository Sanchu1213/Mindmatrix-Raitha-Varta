package com.example.raitha_varta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val preferredCity: StateFlow<String> = repository.preferredCity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "Bangalore"
        )

    val preferredLanguage: StateFlow<String> = repository.preferredLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "en"
        )

    val preferredCrop: StateFlow<String> = repository.preferredCrop
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = "All"
        )

    val alertsEnabled: StateFlow<Boolean> = repository.alertsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val notificationsEnabled: StateFlow<Boolean> = repository.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    val isDarkMode: StateFlow<Boolean?> = repository.isDarkMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateCity(city: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.savePreferredCity(city)
        }
    }

    fun updateDarkMode(enabled: Boolean?) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveDarkMode(enabled)
        }
    }

    fun updateLanguage(langCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.savePreferredLanguage(langCode)
        }
    }

    fun updateCrop(crop: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.savePreferredCrop(crop)
        }
    }

    fun updateAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveAlertsEnabled(enabled)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveNotificationsEnabled(enabled)
        }
    }
}
