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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
                println("🛑 Network Error: ${e.message}")

                val localMessages = try {
                    chatDao.getAllEntities().firstOrNull()?.toList()?.map { it.toDomain() }.orEmpty().also {
                        println("📥 Room FETCH: ${it.size} messages")
                    }
                } catch (roomError: Exception) {
                    println("❌ Room fetch ERROR: ${roomError.message}")
                    emptyList()
                }

                val filtered = localMessages.filterNot {
                    it.botResponse?.contains("offline", ignoreCase = true) == true
                }

                val notice = ChatMessage(
                    text = null,
                    isUser = false,
                    botResponse = "🔌 Please reconnect to internet, loaded offline mode",
                    fileName = null,
                    userToken = null
                )

                _messages.value = filtered + notice
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
            println("🔌 OFFLINE MODE: saving to Room")
            saveMessageOffline(userMessage)

            val notice = ChatMessage(
                text = null,
                isUser = false,
                botResponse = "🔌 Please reconnect to internet, loaded offline mode",
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
                println("❌ Network failure during send: ${e.message}")
                saveMessageOffline(userMessage)

                val notice = ChatMessage(
                    text = null,
                    isUser = false,
                    botResponse = "🔌 Please reconnect to internet, loaded offline mode",
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
            println("🔔 Connection lost")

            val alreadyHasNotice = _messages.value.any {
                it.botResponse?.contains("offline", ignoreCase = true) == true &&
                        it.isUser == false &&
                        it.text.isNullOrBlank()
            }

            if (!alreadyHasNotice) {
                val notice = ChatMessage(
                    text = null,
                    isUser = false,
                    botResponse = "🔌 Please reconnect to internet, loaded offline mode",
                    fileName = null,
                    userToken = null
                )
                _messages.value += notice
                saveMessageOffline(notice)
            }
        } else if (isConnected && !lastConnectionStatus) {
            println("🔔 Connection restored")

            _messages.value = _messages.value.filterNot {
                it.botResponse?.contains("offline", ignoreCase = true) == true ||
                        (it.isUser && it.botResponse.isNullOrBlank())
            }

            viewModelScope.launch {
                try {
                    val restored = getMessages()
                    _messages.value = restored
                } catch (e: Exception) {
                    println("❌ Failed to refresh after reconnect: ${e.message}")
                }
            }
        }
        lastConnectionStatus = isConnected
    }



    fun saveMessageOffline(message: ChatMessage) {
        viewModelScope.launch {
            try {
                val id = chatDao.insert(ChatEntity.fromDomain(message))
                println("💾 Room INSERT: $id | ${message.text ?: message.botResponse}")
            } catch (e: Exception) {
                println("❌ Room insert ERROR: ${e.message}")
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

    private fun offlineSystemMessage(botText: String) = ChatMessage(
        text = null,
        isUser = false,
        botResponse = botText,
        fileName = null,
        userToken = null,
    )
}
