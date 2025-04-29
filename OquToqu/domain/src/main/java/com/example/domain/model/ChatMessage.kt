package com.example.domain.model

data class ChatMessage(
    val id: Long,
    val text: String?,
    val fileName: String?,
    val isUser: Boolean,
    val botResponse: String?
)