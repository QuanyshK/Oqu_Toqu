package com.example.domain.usecase

import com.example.domain.repository.ChatRepository

class SendChatMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(text: String?, fileUri: String?) {
        repository.sendMessage(text, fileUri)
    }
}