package com.example.oqutoqu.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ChatMessage
import com.example.domain.usecase.GetChatMessagesUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val context: Context,
    private val getMessages: GetChatMessagesUseCase,
    private val sendMessage: SendChatMessageUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    var selectedFileUri: Uri? = null
        private set

    var selectedFileName by mutableStateOf<String?>(null)
        private set

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            _messages.value = getMessages()
        }
    }

    fun onSend(text: String) {
        if (text.isBlank() && selectedFileUri == null) return

        val userText = if (selectedFileUri != null) {
            selectedFileName ?: "Attached file"
        } else {
            text
        }

        val newMessage = ChatMessage(
            id = System.currentTimeMillis(),
            text = userText,
            fileName = selectedFileName,
            isUser = true,
            botResponse = ""
        )

        _messages.value = _messages.value + newMessage

        viewModelScope.launch {
            try {
                val sendingText = if (selectedFileUri != null) "Uploading file..." else text
                sendMessage.invoke(sendingText, selectedFileUri?.toString())

                _messages.value = getMessages()
            } catch (e: Exception) {
            }

            clearSelectedFile()
        }
    }



    fun setSelectedFile(uri: Uri) {
        selectedFileUri = uri
        selectedFileName = getFileNameFromUri(context, uri)
    }

    fun clearSelectedFile() {
        selectedFileUri = null
        selectedFileName = null
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index != -1) {
                it.getString(index) ?: uri.lastPathSegment ?: "file"
            } else uri.lastPathSegment ?: "file"
        } ?: uri.lastPathSegment ?: "file"
    }
}
