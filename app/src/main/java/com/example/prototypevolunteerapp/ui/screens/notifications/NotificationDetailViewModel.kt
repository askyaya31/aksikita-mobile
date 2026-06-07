package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.model.NotificationItems
import com.example.prototypevolunteerapp.data.model.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class NotificationDetailUiState(
    val notification: NotificationItems? = null
)

@HiltViewModel
class NotificationDetailViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private var notificationId: Int = -1

    fun init(id: Int) {
        notificationId = id
        notificationRepository.markAsRead(id)
    }

    val uiState: StateFlow<NotificationDetailUiState> =
        notificationRepository.notifications
            .map { list ->
                NotificationDetailUiState(notification = list.find { it.id == notificationId })
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = NotificationDetailUiState()
            )

    init {
        notificationRepository.markAsRead(notificationId)
    }
}