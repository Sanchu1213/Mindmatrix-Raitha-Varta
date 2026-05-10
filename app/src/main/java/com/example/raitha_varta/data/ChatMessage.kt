package com.example.raitha_varta.data

import android.graphics.Bitmap
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val image: Bitmap? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class Consultation(
    val id: String = UUID.randomUUID().toString(),
    val lastMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Open"
)
