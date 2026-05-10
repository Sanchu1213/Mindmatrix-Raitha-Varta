package com.example.raitha_varta.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raitha_varta.data.Scheme
import com.example.raitha_varta.data.SchemeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SchemeViewModel @Inject constructor(
    private val repository: SchemeRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val schemes: StateFlow<List<Scheme>> = combine(
        repository.getAllSchemes(),
        _searchQuery
    ) { schemes, query ->
        if (query.isEmpty()) {
            schemes
        } else {
            schemes.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) 
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun refreshSchemes() {
        viewModelScope.launch {
            val mockSchemes = listOf(
                Scheme(
                    id = "1",
                    title = "PM-KISAN",
                    description = "Income support of Rs. 6,000/- per year in three equal installments will be provided to all land holding farmer families.",
                    eligibility = "All land holding farmer families.",
                    benefits = "Financial assistance of Rs. 6,000 per year.",
                    howToApply = "Register on PM-KISAN portal or via CSC centres.",
                    sourceUrl = "https://pmkisan.gov.in/",
                    category = "Financial Support"
                ),
                Scheme(
                    id = "2",
                    title = "Pradhan Mantri Fasal Bima Yojana (PMFBY)",
                    description = "Yield based crop insurance scheme for farmers.",
                    eligibility = "All farmers including sharecroppers and tenant farmers.",
                    benefits = "Insurance cover against crop failure.",
                    howToApply = "Apply through banks or PMFBY portal.",
                    sourceUrl = "https://pmfby.gov.in/",
                    category = "Insurance"
                ),
                Scheme(
                    id = "3",
                    title = "Kisan Credit Card (KCC)",
                    description = "Provides farmers with timely access to credit.",
                    eligibility = "All farmers, individuals or joint borrowers.",
                    benefits = "Easy access to loans at lower interest rates.",
                    howToApply = "Visit any commercial or co-operative bank.",
                    sourceUrl = "https://pib.gov.in/",
                    category = "Credit"
                ),
                Scheme(
                    id = "4",
                    title = "Soil Health Card Scheme",
                    description = "Provides farmers with crop-wise recommendations of nutrients and fertilizers.",
                    eligibility = "All farmers are eligible to receive Soil Health Cards.",
                    benefits = "Helps in understanding soil health and using fertilizers judiciously.",
                    howToApply = "Visit local agriculture office or soil testing lab.",
                    sourceUrl = "https://soilhealth.dac.gov.in/",
                    category = "Sustainable Farming"
                )
            )
            repository.insertSchemes(mockSchemes)
        }
    }

    init {
        refreshSchemes()
    }
}
