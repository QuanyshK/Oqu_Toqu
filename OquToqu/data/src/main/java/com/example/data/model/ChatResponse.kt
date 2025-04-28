package com.example.data.model

import com.example.domain.model.ChatMessage


data class ChatResponse(
    val id: Int,
    val user: Int,
    val user_message: String?,
    val bot_response: String?,
    val file_name: String?,
) {
    fun toDomain(isUserMessage: Boolean): ChatMessage {
        return ChatMessage(
            id = id.toLong(),
            text = if (isUserMessage) user_message else bot_response,
            fileName = file_name,
            isUser = isUserMessage
        )
    }
}
