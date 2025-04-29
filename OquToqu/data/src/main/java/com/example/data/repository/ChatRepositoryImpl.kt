package com.example.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
    private val authApi: AuthApi
) : ChatRepository {

    override suspend fun getMessages(): List<ChatMessage> {
        val response = authApi.getMessages()
        val body = response.body()
        return body?.messages?.flatMap { listOf(
            it.toDomain(isUserMessage = true),
            it.toDomain(isUserMessage = false)
        ) } ?: emptyList()
    }

    override suspend fun sendMessage(text: String?, fileUri: String?): ChatMessage {
        val realText = (text ?: "").ifBlank { "Uploading file..." }
        val textPart = realText.toRequestBody("text/plain".toMediaTypeOrNull())

        var filePart: MultipartBody.Part? = null
        var fileNamePart: RequestBody? = null

        if (!fileUri.isNullOrEmpty()) {
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

        val response = authApi.sendMessage(textPart, filePart, fileNamePart)

        if (response.isSuccessful) {
            return response.body()?.toDomain(isUserMessage = true)
                ?: throw Exception("Empty response")
        } else {
            throw Exception("Failed to send message: ${response.code()}")
        }
    }


    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open input stream from URI: $uri")
        val fileName = getFileNameFromUri(context, uri)
        val tempFile = File(context.cacheDir, fileName)
        inputStream.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index != -1) {
                it.getString(index)
            } else {
                uri.lastPathSegment ?: "file"
            }
        } ?: uri.lastPathSegment ?: "file"
    }

}
