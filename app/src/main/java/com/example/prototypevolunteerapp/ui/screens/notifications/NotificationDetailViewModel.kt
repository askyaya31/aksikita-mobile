package com.example.prototypevolunteerapp.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import com.example.prototypevolunteerapp.data.remote.dto.NotificationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotifDetailUiState(
    val notification:    NotificationDto? = null,
    val isLoading:       Boolean          = true,
    val error:           String?          = null,
    val isLoadingEvent:  Boolean          = false,
    val relatedEvent:    EventDto?        = null,
    val eventNotFound:   Boolean          = false
)

@HiltViewModel
class NotifDetailViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val organizerSession: OrganizerSession
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotifDetailUiState())
    val uiState: StateFlow<NotifDetailUiState> = _uiState.asStateFlow()

    private val isOrg get() = organizerSession.isLoggedIn

    fun init(notificationId: Int) {
        viewModelScope.launch {
            _uiState.value = NotifDetailUiState(isLoading = true)
            try {
                val resp = if (isOrg) apiService.getOrgNotifications()
                else       apiService.getVolunteerNotifications()

                if (resp.isSuccessful) {
                    val found = resp.body()?.data?.find { it.id == notificationId }
                    _uiState.value = NotifDetailUiState(
                        notification = found,
                        isLoading    = false,
                        error        = if (found == null) "Notifikasi tidak ditemukan" else null
                    )
                    if (found != null) {
                        try {
                            if (isOrg) apiService.markOrgNotificationRead(notificationId)
                            else       apiService.markVolunteerNotificationRead(notificationId)
                        } catch (_: Exception) {}
                    }
                } else {
                    _uiState.value = NotifDetailUiState(
                        isLoading = false,
                        error     = "Gagal memuat notifikasi (${resp.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = NotifDetailUiState(
                    isLoading = false,
                    error     = "Tidak dapat terhubung ke server."
                )
            }
        }
    }

    fun fetchRelatedEvent(eventId: Int, onFound: (EventDto) -> Unit, onNotFound: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEvent = true, eventNotFound = false)
            try {
                val resp = apiService.getPublicEvents(search = eventId.toString())
                if (resp.isSuccessful && resp.body() != null) {
                    val event = resp.body()!!.data.firstOrNull { it.id == eventId }
                    if (event != null) {
                        _uiState.value = _uiState.value.copy(
                            isLoadingEvent = false,
                            relatedEvent   = event
                        )
                        onFound(event)
                    } else {
                        // Event tidak ketemu di search, fallback ke activities
                        _uiState.value = _uiState.value.copy(
                            isLoadingEvent = false,
                            eventNotFound  = true
                        )
                        onNotFound()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingEvent = false, eventNotFound = true)
                    onNotFound()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoadingEvent = false, eventNotFound = true)
                onNotFound()
            }
        }
    }
}