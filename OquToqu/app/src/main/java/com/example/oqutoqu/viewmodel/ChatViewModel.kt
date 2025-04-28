package com.example.oqutoqu.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ChatMessage
import com.example.domain.usecase.GetChatMessagesUseCase
import com.example.domain.usecase.SendChatMessageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val getMessages: GetChatMessagesUseCase,
    private val sendMessage: SendChatMessageUseCase
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init { loadMessages() }

    private fun loadMessages() {
        viewModelScope.launch {
            _messages.value = getMessages()
        }
    }

    fun onSend(text: String, filePath: String? = null) {
        if (text.isBlank() && filePath == null) return
        viewModelScope.launch {
            sendMessage(text, filePath)
            _messages.value = getMessages()
        }
    }
}