package com.example.raitha_varta.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schemes")
data class Scheme(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val eligibility: String,
    val benefits: String,
    val howToApply: String,
    val sourceUrl: String?,
    val category: String // e.g., "Subsidy", "Insurance", "Credit"
)
