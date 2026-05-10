package com.example.raitha_varta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.R
import com.example.raitha_varta.data.Crop
import com.example.raitha_varta.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Full curated list of crops using local images
    private val allCrops = listOf(
        // CEREALS & MILLETS
        Crop("paddy", "Paddy (Rice)", R.drawable.rice, "Cereals", "KHARIF", true, 0.4f),
        Crop("wheat", "Wheat", R.drawable.wheat, "Cereals", "RABI", false, 0.3f),
        Crop("ragi", "Ragi (Millet)", R.drawable.ragi, "Millets", "KHARIF", false, 0.5f),
        Crop("maize", "Maize (Corn)", R.drawable.corn, "Cereals", "RABI/KHARIF", false, 0.2f),
        Crop("bajra", "Bajra", R.drawable.bajra, "Millets", "KHARIF", false, 0.1f),
        Crop("jowar", "Jowar", R.drawable.jowar, "Millets", "KHARIF/RABI", false, 0.1f),

        // VEGETABLES
        Crop("tomato", "Tomato", R.drawable.tomato, "Vegetables", "ALL YEAR", true, 0.6f),
        Crop("onion", "Onion", R.drawable.onion, "Vegetables", "RABI", false, 0.7f),
        Crop("potato", "Potato", R.drawable.potato, "Vegetables", "RABI", false, 0.8f),
        Crop("brinjal", "Brinjal", R.drawable.brinjal, "Vegetables", "ALL YEAR", false, 0.4f),
        Crop("okra", "Okra", R.drawable.lady_finger, "Vegetables", "SUMMER/KHARIF", false, 0.3f),
        Crop("carrot", "Carrot", R.drawable.carrot, "Vegetables", "WINTER", false, 0.4f),
        Crop("spinach", "Spinach", R.drawable.spinach, "Vegetables", "ALL YEAR", false, 0.3f),
        Crop("drumstick", "Drumstick", R.drawable.drumstick, "Vegetables", "PERENNIAL", false, 0.4f),
        Crop("cabbage", "Cabbage", R.drawable.cabbage, "Vegetables", "WINTER", false, 0.5f),
        Crop("red_chilli", "Chilli", R.drawable.red_chilli, "Vegetables", "KHARIF/RABI", false, 0.5f),

        // FRUITS
        Crop("mango", "Mango", R.drawable.mango, "Fruits", "SUMMER", false, 0.9f),
        Crop("banana", "Banana", R.drawable.banana, "Fruits", "ANNUAL", false, 0.8f),
        Crop("pomegranate", "Pomegranate", R.drawable.pomegranate, "Fruits", "ALL YEAR", false, 0.5f),
        Crop("grapes", "Grapes", R.drawable.grapes, "Fruits", "SUMMER", false, 0.7f),
        Crop("papaya", "Papaya", R.drawable.papaya, "Fruits", "ANNUAL", false, 0.6f),

        // PLANTATION
        Crop("coconut", "Coconut", R.drawable.coconuts, "Plantation", "SUMMER", false, 1.0f),
        Crop("arecanut", "Areca nut", R.drawable.arecanut, "Plantation", "PERENNIAL", false, 0.9f),
        Crop("sugarcane", "Sugarcane", R.drawable.sugarcane, "Plantation", "ANNUAL", false, 0.2f),
        Crop("coffee", "Coffee", R.drawable.coffee, "Plantation", "PERENNIAL", false, 0.9f)
    )

    val crops = _searchQuery
        .map { query ->
            if (query.isBlank()) allCrops
            else allCrops.filter { it.name.contains(query, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), allCrops)

    private val _selectedCrop = MutableStateFlow<Crop?>(null)
    val selectedCrop = _selectedCrop.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.preferredCrop.collect { name ->
                _selectedCrop.value = allCrops.find { it.name == name } ?: allCrops.firstOrNull()
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectCrop(crop: Crop) {
        _selectedCrop.value = crop
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.savePreferredCrop(crop.name)
        }
    }
}
