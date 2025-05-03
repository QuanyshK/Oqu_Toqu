package com.example.domain.usecase

import com.example.domain.repository.ChatRepository

class GetChatMessagesUseCaseTest(
    private val repository: ChatRepository
) {
    suspend operator fun invoke() = repository.getMessages()
}