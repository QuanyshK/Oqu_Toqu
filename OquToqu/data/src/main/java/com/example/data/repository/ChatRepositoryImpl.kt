package com.example.data.repository

import com.example.data.source.local.ChatDao
import com.example.data.source.local.ChatEntity
import com.example.data.source.remote.AuthApi
import com.example.domain.model.ChatMessage
import com.example.domain.repository.ChatRepository
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ChatRepositoryImpl(
    private val dao: ChatDao,
    private val api: AuthApi
) : ChatRepository {

    override suspend fun getAllMessages(): List<ChatMessage> {
        val entities = dao.getAllEntities().first()
        return entities.map { it.toDomain() }
    }

    override suspend fun sendMessage(text: String, filePath: String?): String {
        val userEntity = ChatEntity(
            text = text.takeIf(String::isNotBlank),
            fileName = filePath?.substringAfterLast('/'),
            isUser = true
        )
        dao.insert(userEntity)

        val textBody = text
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val filePart = filePath
            ?.let { path ->
                val file = File(path)
                val mime = when {
                    path.endsWith(".pdf")  -> "application/pdf"
                    path.endsWith(".docx")-> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    else                    -> "application/octet-stream"
                }
                file
                    .asRequestBody(mime.toMediaTypeOrNull())
                    .let { rb ->
                        MultipartBody.Part.createFormData("file", file.name, rb)
                    }
            }

        val botText = try {
            val resp = api.sendAndReceive(textBody, filePart)
            resp.botResponse
        } catch (e: Exception) {
            "Error: ${e.localizedMessage}"
        }

        dao.insert(
            ChatEntity(
                text = botText,
                fileName = null,
                isUser = false
            )
        )

        return botText
    }
}