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
    val isUser: Boolean
) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id,
        text = text,
        fileName = fileName,
        isUser = isUser
    )
}