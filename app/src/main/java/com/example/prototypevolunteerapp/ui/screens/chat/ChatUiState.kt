package com.example.prototypevolunteerapp.ui.screens.chat

import com.example.prototypevolunteerapp.data.remote.dto.ChatRoomDto
import com.example.prototypevolunteerapp.data.remote.dto.ChatMessageDto

data class ChatListUiState(
    val rooms: List<ChatRoomDto> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class ChatRoomUiState(
    val messages: List<ChatMessageDto> = emptyList(),
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val inputText: String = "",
    val isOpen: Boolean = true,
    val error: String? = null
)