package com.example.oqutoqu.viewmodel

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.manager.AuthManager
import com.example.data.source.local.ChatDao
import com.example.data.source.local.ChatEntity
import com.example.domain.model.ChatMessage
import com.example.domain.usecase.GetChatMessagesUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import com.example.oqutoqu.receiver.NetworkReceiver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ChatViewModel(
    private val context: Context,
    private val getMessages: GetChatMessagesUseCase,
    private val sendMessage: SendChatMessageUseCase,
    private val chatDao: ChatDao
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val authManager = AuthManager(context)
    private var lastConnectionStatus: Boolean = true

    var selectedFileUri: Uri? = null
        private set

    var selectedFileName by mutableStateOf<String?>(null)
        private set

    init {
        loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            try {
                _messages.value = getMessages()
            } catch (e: Exception) {
                println("Network Error: ${e.message}")

                val currentToken = authManager.getToken()
                val localMessages = try {
                    chatDao.getAllEntities().firstOrNull()?.map { it.toDomain() }?.filter {
                        it.userToken == currentToken
                    }?.also {
                    } ?: emptyList()
                } catch (roomError: Exception) {
                    emptyList()
                }

                val notice = ChatMessage(
                    text = null,
                    isUser = false,
                    botResponse = "Please reconnect to internet, loaded offline mode",
                    fileName = null,
                    userToken = currentToken
                )

                _messages.value = localMessages + notice
            }
        }
    }



    fun onSend(text: String) {
        if (text.isBlank() && selectedFileUri == null) return

        val isConnected = NetworkReceiver.isConnected(context)
        val userText = selectedFileName.takeIf { selectedFileUri != null } ?: text
        val userToken = authManager.getToken()

        val userMessage = ChatMessage(
            text = userText,
            fileName = selectedFileName,
            isUser = true,
            botResponse = "",
            userToken = userToken
        )

        _messages.value = _messages.value.filterNot { it.botResponse?.contains("offline", ignoreCase = true) == true }

        _messages.value += userMessage

        if (!isConnected) {
            saveMessageOffline(userMessage)

            val notice = ChatMessage(
                text = null,
                isUser = false,
                botResponse = "Please reconnect to internet, loaded offline mode",
                fileName = null,
                userToken = null
            )
            _messages.value += notice
            saveMessageOffline(notice)
            clearSelectedFile()
            return
        }

        viewModelScope.launch {
            try {
                val content = if (selectedFileUri != null) "Uploading file..." else text
                sendMessage(content, selectedFileUri?.toString())
                val updated = getMessages()
                _messages.value = updated
                updated.forEach { saveMessageOffline(it) }
            } catch (e: Exception) {
                saveMessageOffline(userMessage)

                val notice = ChatMessage(
                    text = null,
                    isUser = false,
                    botResponse = "Please reconnect to internet, loaded offline mode",
                    fileName = null,
                    userToken = null
                )
                _messages.value += notice
                saveMessageOffline(notice)
            }
            clearSelectedFile()
        }
    }

    fun onConnectionChanged(isConnected: Boolean) {
        if (!isConnected && lastConnectionStatus) {

            val alreadyHasNotice = _messages.value.any {
                it.botResponse?.contains("offline", ignoreCase = true) == true &&
                        it.isUser == false &&
                        it.text.isNullOrBlank()
            }

            if (!alreadyHasNotice) {
                val notice = ChatMessage(
                    text = null,
                    isUser = false,
                    botResponse = "Please reconnect to internet, loaded offline mode",
                    fileName = null,
                    userToken = null
                )
                _messages.value += notice
                saveMessageOffline(notice)
            }
        } else if (isConnected && !lastConnectionStatus) {

            _messages.value = _messages.value.filterNot {
                it.botResponse?.contains("offline", ignoreCase = true) == true ||
                        (it.isUser && it.botResponse.isNullOrBlank())
            }

            viewModelScope.launch {
                try {
                    val restored = getMessages()
                    _messages.value = restored
                } catch (e: Exception) {
                    println("Failed to refresh after reconnect: ${e.message}")
                }
            }
        }
        lastConnectionStatus = isConnected
    }



    fun saveMessageOffline(message: ChatMessage) {
        viewModelScope.launch {
            try {
                val id = chatDao.insert(ChatEntity.fromDomain(message))
            } catch (e: Exception) {
            }
        }
    }

    fun setSelectedFile(uri: Uri) {
        selectedFileUri = uri
        selectedFileName = getFileNameFromUri(uri)
    }

    fun clearSelectedFile() {
        selectedFileUri = null
        selectedFileName = null
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (it.moveToFirst() && index != -1)
                it.getString(index) ?: uri.lastPathSegment ?: "file"
            else uri.lastPathSegment ?: "file"
        } ?: uri.lastPathSegment ?: "file"
    }
}
