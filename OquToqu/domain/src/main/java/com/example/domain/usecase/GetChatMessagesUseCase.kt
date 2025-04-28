package com.example.domain.usecase

import com.example.domain.model.ChatMessage
import com.example.domain.repository.ChatRepository

class GetChatMessagesUseCase(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(): List<ChatMessage> =
        repo.getAllMessages()
}