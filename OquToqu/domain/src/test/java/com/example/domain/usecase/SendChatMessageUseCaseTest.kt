package com.example.domain.usecase

import com.example.domain.repository.ChatRepository

class SendChatMessageUseCaseTest(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(text: String?, fileName: String?) {
        repository.sendMessage(text, fileName)
    }
}
