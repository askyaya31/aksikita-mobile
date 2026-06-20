package com.example.prototypevolunteerapp.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.SendMessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _listState = MutableStateFlow(ChatListUiState())
    val listState = _listState.asStateFlow()

    private var isOrganizer = false

    fun loadChatRooms(asOrganizer: Boolean = false) = viewModelScope.launch {
        isOrganizer = asOrganizer
        _listState.update { it.copy(isLoading = true) }
        runCatching {
            if (asOrganizer) api.getOrgChatRooms() else api.getChatRooms()
        }
            .onSuccess { rooms -> _listState.update { it.copy(rooms = rooms, isLoading = false) } }
            .onFailure { e    -> _listState.update { it.copy(error = e.message, isLoading = false) } }
    }

    private val _roomState = MutableStateFlow(ChatRoomUiState())
    val roomState = _roomState.asStateFlow()

    private var pollJob: Job? = null
    private var lastMessageId = 0

    fun loadMessages(roomId: Int, asOrganizer: Boolean = false) = viewModelScope.launch {
        isOrganizer = asOrganizer
        _roomState.update { it.copy(isLoading = true) }
        runCatching {
            if (asOrganizer) api.getOrgChatMessages(roomId) else api.getChatMessages(roomId)
        }
            .onSuccess { msgs ->
                lastMessageId = msgs.lastOrNull()?.id ?: 0
                _roomState.update { it.copy(messages = msgs, isLoading = false) }
            }
            .onFailure { e -> _roomState.update { it.copy(error = e.message, isLoading = false) } }
    }

    fun startPolling(roomId: Int) {
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(3_000L)
                runCatching {
                    if (isOrganizer) api.pollOrgMessages(roomId, lastMessageId)
                    else api.pollMessages(roomId, lastMessageId)
                }
                    .onSuccess { resp ->
                        if (resp.messages.isNotEmpty()) {
                            lastMessageId = resp.messages.last().id
                            _roomState.update { s -> s.copy(messages = s.messages + resp.messages) }
                        }
                    }
            }
        }
    }

    fun stopPolling() { pollJob?.cancel() }

    fun onInputChange(text: String) {
        _roomState.update { it.copy(inputText = text) }
    }

    fun sendMessage(roomId: Int) = viewModelScope.launch {
        val text = _roomState.value.inputText.trim()
        if (text.isBlank()) return@launch
        _roomState.update { it.copy(isSending = true, inputText = "") }
        runCatching {
            if (isOrganizer) api.sendOrgMessage(roomId, SendMessageRequest(text))
            else api.sendMessage(roomId, SendMessageRequest(text))
        }
            .onSuccess { msg ->
                lastMessageId = msg.id
                _roomState.update { s -> s.copy(messages = s.messages + msg, isSending = false) }
            }
            .onFailure { _roomState.update { it.copy(isSending = false) } }
    }
}