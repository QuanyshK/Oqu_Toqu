package com.example.domain.usecase

import com.example.domain.repository.ChatRepository

class SendChatMessageUseCase(
    private val repo: ChatRepository
) {
    suspend operator fun invoke(text: String, filePath: String? = null) =
        repo.sendMessage(text, filePath)
}