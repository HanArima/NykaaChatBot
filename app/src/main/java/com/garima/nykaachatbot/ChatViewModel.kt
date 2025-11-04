package com.garima.nykaachatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(userInput: String) {
        val userMessage = Message(userInput, isUser = true)
        _messages.value = _messages.value + userMessage

        _isLoading.value = true

        viewModelScope.launch {
            try {
                val responseText = GeminiService.getGeminiResponse(userInput)
                val aiMessage = Message(responseText, isUser = false)
                _messages.value = _messages.value + aiMessage
            } catch (e: Exception) {
                val errorMessage = Message("Error: ${e.message}", isUser = false)
                _messages.value = _messages.value + errorMessage
            } finally {
                _isLoading.value = false
            }
        }
    }
}
