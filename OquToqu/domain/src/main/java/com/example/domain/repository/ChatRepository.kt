package com.example.domain.repository

import com.example.domain.model.ChatMessage


interface ChatRepository {
    suspend fun getAllMessages(): List<ChatMessage>

    suspend fun sendMessage(text: String, filePath: String? = null): String
}
