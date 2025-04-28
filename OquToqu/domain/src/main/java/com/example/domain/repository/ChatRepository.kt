package com.example.domain.repository

import com.example.domain.model.ChatMessage

interface ChatRepository {
    suspend fun getMessages(): List<ChatMessage>
    suspend fun sendMessage(text: String?, fileUri: String?): ChatMessage
}
