package com.example.data.model

import com.example.domain.model.ChatMessage

data class ChatResponse(
    val id: Int,
    val user: Int,
    val user_message: String?,
    val bot_response: String?,
    val file_name: String?
) {
    fun toDomain(isUserMessage: Boolean, userToken: String): ChatMessage {
        return ChatMessage(
            id = id.toLong(),
            text = if (isUserMessage) {
                if (!file_name.isNullOrEmpty()) file_name else user_message
            } else {
                bot_response
            },
            fileName = file_name,
            botResponse = if (isUserMessage) null else bot_response,
            isUser = isUserMessage,
            userToken = userToken,
        )
    }
}
