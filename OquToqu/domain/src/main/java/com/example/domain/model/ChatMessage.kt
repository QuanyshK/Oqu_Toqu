package com.example.domain.model

data class ChatMessage(
    val id: Long? = null,
    val text: String?,
    val fileName: String?,
    val botResponse: String?,
    val isUser: Boolean,
    val userToken: String?
)