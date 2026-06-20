package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EventDetailUiState(
    val event:         EventDto?              = null,
    val registrations: List<RegistrationDto>  = emptyList(),
    val isLoading:     Boolean                = false,
    val error:         String?                = null,
    val actionMessage: String?                = null
) {
    val menungguCount     get() = registrations.count { it.status == "pending" }
    val dikonfirmasiCount get() = registrations.count { it.status == "confirmed" }
    val hadirCount        get() = registrations.count { it.status == "attended" }
    val dibatalkanCount   get() = registrations.count { it.status == "cancelled" }
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val organizerSession: OrganizerSession
) : ViewModel() {

    fun checkAccess(): Boolean = organizerSession.hasAccess()

    private val _uiState = MutableStateFlow(EventDetailUiState())
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private var currentEventId: Int = 0

    fun loadData(eventId: Int) {
        currentEventId = eventId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val eventResp = apiService.getOrgEventDetail(eventId)
                val regResp   = apiService.getEventRegistrations(eventId = eventId)

                val event = if (eventResp.isSuccessful) eventResp.body()?.event else null
                val regs  = if (regResp.isSuccessful) regResp.body()?.data ?: emptyList() else emptyList()

                _uiState.value = _uiState.value.copy(
                    event         = event,
                    registrations = regs,
                    isLoading     = false,
                    error         = if (event == null) "Gagal memuat detail kegiatan (${eventResp.code()})" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun refreshRegistrations() {
        viewModelScope.launch {
            try {
                val resp = apiService.getEventRegistrations(eventId = currentEventId)
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(registrations = resp.body()?.data ?: emptyList())
                }
            } catch (_: Exception) { }
        }
    }

    fun onConfirmRegistration(registrationId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.confirmRegistration(registrationId)
                if (resp.isSuccessful) {
                    refreshRegistrations()
                    _uiState.value = _uiState.value.copy(actionMessage = "Kandidat berhasil diterima")
                } else {
                    _uiState.value = _uiState.value.copy(actionMessage = "Gagal menerima (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionMessage = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    fun onRejectRegistration(registrationId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.rejectRegistration(registrationId)
                if (resp.isSuccessful) {
                    refreshRegistrations()
                    _uiState.value = _uiState.value.copy(actionMessage = "Kandidat ditolak")
                } else {
                    _uiState.value = _uiState.value.copy(actionMessage = "Gagal menolak (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionMessage = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    fun onAttendRegistration(registrationId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.attendRegistration(registrationId)
                if (resp.isSuccessful) {
                    refreshRegistrations()
                    _uiState.value = _uiState.value.copy(actionMessage = "Kandidat ditandai hadir")
                } else {
                    _uiState.value = _uiState.value.copy(actionMessage = "Gagal menandai (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionMessage = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    fun onCompleteEvent() {
        viewModelScope.launch {
            try {
                val resp = apiService.completeOrgEvent(currentEventId)
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        event         = _uiState.value.event?.copy(status = "completed"),
                        actionMessage = "Kegiatan telah selesai dilaksanakan."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(actionMessage = "Gagal menyelesaikan kegiatan (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(actionMessage = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    fun onActionMessageHandled() { _uiState.value = _uiState.value.copy(actionMessage = null) }
    fun onErrorDismissed()       { _uiState.value = _uiState.value.copy(error = null) }
}
