package com.example.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ChatMessage

@Entity(tableName = "chat_messages")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String?,
    val fileName: String?,
    val botResponse: String?,
    val isUser: Boolean,
    val userToken: String?,

) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        text = text,
        fileName = fileName,
        botResponse = botResponse,
        isUser = isUser,
        userToken = userToken
    )

    companion object {
        fun fromDomain(message: ChatMessage): ChatEntity {
            return ChatEntity(
                id = 0,
                text = message.text,
                fileName = message.fileName,
                botResponse = message.botResponse,
                isUser = message.isUser,
                userToken = message.userToken ?: ""
            )
        }
    }
}
