package com.example.domain.usecase

import com.example.domain.repository.ChatRepository

class GetChatMessagesUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke() = repository.getMessages()
}