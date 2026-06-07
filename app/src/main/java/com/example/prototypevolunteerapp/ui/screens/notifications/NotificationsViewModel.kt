package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<NotificationDto> = emptyList(),
    val isLoading:     Boolean  = true,
    val errorMessage:  String?  = null,
    val isOrganizer:   Boolean  = false
) {
    val unreadCount: Int     get() = notifications.count { !it.is_read }
    val isEmpty:     Boolean get() = notifications.isEmpty()
    val hasUnread:   Boolean get() = unreadCount > 0
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val apiService:      ApiService,
    private val userSession:     UserSession,
    private val organizerSession: OrganizerSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    private val isOrg get() = organizerSession.isLoggedIn

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState(isLoading = true, isOrganizer = isOrg)
            try {
                val response = if (isOrg) {
                    apiService.getOrgNotifications()
                } else {
                    apiService.getVolunteerNotifications()
                }

                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    _uiState.value = NotificationsUiState(
                        notifications = list,
                        isLoading     = false,
                        isOrganizer   = isOrg
                    )
                } else {
                    _uiState.value = NotificationsUiState(
                        isLoading    = false,
                        isOrganizer  = isOrg,
                        errorMessage = "Gagal memuat notifikasi (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = NotificationsUiState(
                    isLoading    = false,
                    isOrganizer  = isOrg,
                    errorMessage = "Tidak dapat terhubung ke server."
                )
            }
        }
    }

    fun onNotificationOpened(id: Int) {
        _uiState.value = _uiState.value.copy(
            notifications = _uiState.value.notifications.map { notif ->
                if (notif.id == id) notif.copy(is_read = true) else notif
            }
        )
        viewModelScope.launch {
            try {
                if (isOrg) {
                    apiService.markOrgNotificationRead(id)
                } else {
                    apiService.markVolunteerNotificationRead(id)
                }
            } catch (e: Exception) {
                android.util.Log.w("NotifVM", "markRead gagal: ${e.message}")
            }
        }
    }
    fun onMarkAllAsRead() {
        _uiState.value = _uiState.value.copy(
            notifications = _uiState.value.notifications.map { it.copy(is_read = true) }
        )
        viewModelScope.launch {
            try {
                if (isOrg) {
                    apiService.markAllOrgNotificationsRead()
                } else {
                    apiService.markAllVolunteerNotificationsRead()
                }
            } catch (e: Exception) {
                android.util.Log.w("NotifVM", "markAllRead gagal: ${e.message}")
            }
        }
    }
}