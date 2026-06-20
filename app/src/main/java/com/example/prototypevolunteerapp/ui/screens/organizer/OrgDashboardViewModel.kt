package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.model.NotificationRepository
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class OrgTab { DASHBOARD, KEGIATAN, TAMBAH, PESAN, PROFIL }

data class OrgDashboardUiState(
    val events:              List<EventDto> = emptyList(),
    val totalActivities:     Int     = 0,
    val totalCandidates:     Int     = 0,
    val totalSubmissions:    Int     = 0,
    val isLoading:           Boolean = false,
    val error:               String? = null,
    val showStatsSheet:      Boolean = false,
    val sectionsVisible:     Boolean = false,
    val selectedTab:         OrgTab  = OrgTab.DASHBOARD,
    val selectedEventStatus: String? = null,
    val unreadNotifCount:    Int     = 0
) {
    val filteredEvents: List<EventDto>
        get() = if (selectedEventStatus == null) events
        else events.filter { it.status == selectedEventStatus }
}

@HiltViewModel
class OrgDashboardViewModel @Inject constructor(
    private val apiService:             ApiService,
    private val organizerSession:       OrganizerSession,
    private val notificationRepository: NotificationRepository,
    private val sessionPreferences:     SessionPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrgDashboardUiState())
    val uiState: StateFlow<OrgDashboardUiState> = _uiState.asStateFlow()

    private val _cancelTargetId = MutableStateFlow<Int?>(null)
    val cancelTargetId: StateFlow<Int?> = _cancelTargetId.asStateFlow()

    private val _cancelSuccess = MutableStateFlow<String?>(null)
    val cancelSuccess: StateFlow<String?> = _cancelSuccess.asStateFlow()

    val currentOrg get() = organizerSession.currentOrg

    init {
        viewModelScope.launch {
            val saved = sessionPreferences.savedSession.first()
            if (saved != null && saved.role == SessionPreferences.ROLE_ORGANIZER) {
                organizerSession.restoreSession(email = saved.email, name = saved.name)
            }
            loadDashboard()
            loadUnreadCount()
        }
    }

    fun loadData() = loadDashboard()

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                try {
                    val profileResp = apiService.getOrgProfile()
                    if (profileResp.isSuccessful && profileResp.body() != null) {
                        organizerSession.updateLogoUrl(profileResp.body()!!.organization.logo)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("OrgDashboardVM", "Gagal fetch profil: ${e.message}")
                }

                val resp = apiService.getOrgEvents(status = null)

                if (resp.isSuccessful) {
                    val events = resp.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        events          = events,
                        totalActivities = events.size,
                        totalCandidates = events.sumOf { it.registered_count ?: 0 },
                        isLoading       = false,
                        sectionsVisible = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = "Gagal memuat data (${resp.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val resp = apiService.getOrgUnreadCount()
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        unreadNotifCount = resp.body()?.unread_count ?: 0
                    )
                }
            } catch (e: Exception) {
                android.util.Log.w("OrgDashboardVM", "Gagal load unread: ${e.message}")
            }
        }
    }

    fun onNotificationsOpened() {
        _uiState.value = _uiState.value.copy(unreadNotifCount = 0)
    }

    fun onSectionsVisible() {
        _uiState.value = _uiState.value.copy(sectionsVisible = true)
    }

    fun onTabSelected(tab: OrgTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun onEventStatusSelected(status: String?) {
        _uiState.value = _uiState.value.copy(selectedEventStatus = status)
    }

    fun onEventStatusFilterChange(status: String?) = onEventStatusSelected(status)

    fun onStatsSheetShow()    { _uiState.value = _uiState.value.copy(showStatsSheet = true) }
    fun onStatsSheetDismiss() { _uiState.value = _uiState.value.copy(showStatsSheet = false) }

    fun logout() {
        viewModelScope.launch {
            try { apiService.logout() } catch (_: Exception) {}
            sessionPreferences.clearSession()
            organizerSession.logout()
            notificationRepository.clear()
        }
    }

    fun pendingCount(): Int =
        _uiState.value.events.count { it.status == "pending_review" }

    fun acceptedCount(): Int =
        _uiState.value.events.count { it.status == "published" || it.status == "completed" }

    fun rejectedCount(): Int =
        _uiState.value.events.count { it.status == "cancelled" || it.status == "rejected" }

    fun candidateCountFor(eventId: Int): Int =
        _uiState.value.events.find { it.id == eventId }?.registered_count ?: 0

    fun candidateCountFor(title: String): Int =
        _uiState.value.events.find { it.title == title }?.registered_count ?: 0

    fun onCancelEventRequest(eventId: Int) { _cancelTargetId.value = eventId }
    fun onCancelEventDismiss() { _cancelTargetId.value = null }

    fun onCancelEventConfirmed(reason: String?) {
        val eventId = _cancelTargetId.value ?: return
        viewModelScope.launch {
            try {
                val body = if (reason != null) mapOf("reason" to reason) else emptyMap()
                val resp = apiService.cancelOrgEvent(eventId, body)
                if (resp.isSuccessful) {
                    _cancelSuccess.value = "Kegiatan berhasil dibatalkan."
                    val updated = _uiState.value.events.map { ev ->
                        if (ev.id == eventId) ev.copy(status = "cancelled") else ev
                    }
                    _uiState.value = _uiState.value.copy(events = updated)
                } else {
                    _cancelSuccess.value = "Gagal mematalkan (${resp.code()})"
                }
            } catch (e: Exception) {
                _cancelSuccess.value = "Koneksi gagal: ${e.localizedMessage}"
            } finally {
                _cancelTargetId.value = null
            }
        }
    }

    fun onCompleteEvent(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.completeOrgEvent(eventId)
                if (resp.isSuccessful) {
                    _cancelSuccess.value = "Kegiatan telah selesai dilaksanakan."
                    val updated = _uiState.value.events.map { ev ->
                        if (ev.id == eventId) ev.copy(status = "completed") else ev
                    }
                    _uiState.value = _uiState.value.copy(events = updated)
                } else {
                    _cancelSuccess.value = "Gagal menyelesaikan kegiatan (${resp.code()})"
                }
            } catch (e: Exception) {
                _cancelSuccess.value = "Koneksi gagal: ${e.localizedMessage}"
            }
        }
    }

    fun onCancelSuccessHandled() { _cancelSuccess.value = null }
}