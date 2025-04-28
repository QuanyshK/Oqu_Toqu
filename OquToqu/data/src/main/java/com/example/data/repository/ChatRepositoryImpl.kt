package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.source.remote.AuthApi
import com.example.domain.model.ChatMessage
import com.example.domain.repository.ChatRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ChatRepositoryImpl(
    private val context: Context,
    private val chatApi: AuthApi
) : ChatRepository {

    override suspend fun getMessages(): List<ChatMessage> {
        val remoteResponse = chatApi.getMessages()
        val body = remoteResponse.body()

        return body?.messages?.flatMap { msg ->
            listOf(
                msg.toDomain(isUserMessage = true),
                msg.toDomain(isUserMessage = false)
            )
        } ?: emptyList()
    }

    override suspend fun sendMessage(text: String?, fileUri: String?): ChatMessage {
        val realText = (text ?: "").ifBlank { "Uploading file..." }
        val textPart = realText.toRequestBody("text/plain".toMediaTypeOrNull())

        var filePart: MultipartBody.Part? = null
        var fileNamePart: RequestBody? = null

        if (!fileUri.isNullOrBlank()) {
            val file = uriToFile(context, Uri.parse(fileUri))
                ?: throw Exception("Cannot read file from URI: $fileUri")

            val mimeType = when {
                file.name.endsWith(".pdf", true) -> "application/pdf"
                file.name.endsWith(".docx", true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                else -> "application/octet-stream"
            }

            filePart = MultipartBody.Part.createFormData(
                "file", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull())
            )

            fileNamePart = file.name.toRequestBody("text/plain".toMediaTypeOrNull())
        }

        if (textPart == null && filePart == null) {
            throw Exception("Cannot send empty message without file")
        }

        val response = chatApi.sendMessage(textPart, filePart, fileNamePart)

        if (response.isSuccessful) {
            return ChatMessage(
                id = 0,
                text = text,
                fileName = fileUri,
                isUser = true
            )
        } else {
            throw Exception("Failed to send message: ${response.code()}")
        }
    }

    private fun createFilePart(uriString: String): MultipartBody.Part {
        val file = uriToFile(context, Uri.parse(uriString))
            ?: throw Exception("Cannot read file from URI: $uriString")

        val mimeType = when {
            file.name.endsWith(".pdf", true) -> "application/pdf"
            file.name.endsWith(".docx", true) -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "application/octet-stream"
        }

        return MultipartBody.Part.createFormData(
            "file", file.name, file.asRequestBody(mimeType.toMediaTypeOrNull())
        )
    }

    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val tempFile = File.createTempFile("upload", null, context.cacheDir)
            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
