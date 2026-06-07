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

data class CandidateListUiState(
    val events:                List<EventDto>        = emptyList(),
    val selectedEvent:         EventDto?             = null,
    val selectedFilter:        String                = "Semua",
    val allRegistrations:      List<RegistrationDto> = emptyList(),
    val filteredRegistrations: List<RegistrationDto> = emptyList(),
    val isLoading:             Boolean               = false,
    val error:                 String?               = null,
    val showEventSheet:        Boolean               = false,
    val actionSuccess:         String?               = null
)

@HiltViewModel
class CandidateListViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val organizerSession: OrganizerSession
) : ViewModel() {

    fun checkAccess(): Boolean = organizerSession.hasAccess()

    private val _uiState = MutableStateFlow(CandidateListUiState())
    val uiState: StateFlow<CandidateListUiState> = _uiState.asStateFlow()
    fun loadEvents(initialEventId: Int? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resp = apiService.getOrgEvents()
                if (!resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal memuat kegiatan (${resp.code()})"
                    )
                    return@launch
                }
                val events = resp.body()?.data ?: emptyList()
                val startEvent = events.find { it.id == initialEventId } ?: events.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    events    = events,
                    isLoading = false
                )
                startEvent?.let { loadRegistrations(it) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }
    private fun loadRegistrations(event: EventDto) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedEvent = event)
            try {
                val resp = apiService.getEventRegistrations(eventId = event.id)
                val registrations = if (resp.isSuccessful) resp.body()?.data ?: emptyList() else emptyList()
                _uiState.value = _uiState.value.copy(
                    allRegistrations      = registrations,
                    filteredRegistrations = applyFilter(registrations, _uiState.value.selectedFilter),
                    isLoading             = false,
                    selectedFilter        = "Semua"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal memuat kandidat: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onEventSelected(event: EventDto) {
        _uiState.value = _uiState.value.copy(showEventSheet = false, selectedFilter = "Semua")
        loadRegistrations(event)
    }
    fun onFilterSelected(filter: String) {
        _uiState.value = _uiState.value.copy(
            selectedFilter        = filter,
            filteredRegistrations = applyFilter(_uiState.value.allRegistrations, filter)
        )
    }

    private fun applyFilter(list: List<RegistrationDto>, filter: String): List<RegistrationDto> =
        when (filter) {
            "Pending"   -> list.filter { it.status == "pending" }
            "Diterima"  -> list.filter { it.status == "confirmed" }
            "Ditolak"   -> list.filter { it.status == "cancelled" }
            "Hadir"     -> list.filter { it.status == "attended" }
            else        -> list
        }

    fun onConfirmRegistration(registrationId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.confirmRegistration(registrationId)
                if (resp.isSuccessful) {
                    refreshCurrentRegistrations()
                    _uiState.value = _uiState.value.copy(actionSuccess = "Kandidat berhasil diterima")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Gagal menerima (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    fun onRejectRegistration(registrationId: Int, reason: String? = null) {
        viewModelScope.launch {
            try {
                val body = if (reason != null) mapOf("reason" to reason) else emptyMap()
                val resp = apiService.rejectRegistration(registrationId, body)
                if (resp.isSuccessful) {
                    refreshCurrentRegistrations()
                    _uiState.value = _uiState.value.copy(actionSuccess = "Kandidat ditolak")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Gagal menolak (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    fun onAttendRegistration(registrationId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.attendRegistration(registrationId)
                if (resp.isSuccessful) {
                    refreshCurrentRegistrations()
                    _uiState.value = _uiState.value.copy(actionSuccess = "Kandidat ditandai hadir")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Gagal menandai (${resp.code()})")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Koneksi gagal: ${e.localizedMessage}")
            }
        }
    }

    private fun refreshCurrentRegistrations() {
        val event = _uiState.value.selectedEvent ?: return
        loadRegistrations(event)
    }

    fun onEventSheetShow()    { _uiState.value = _uiState.value.copy(showEventSheet = true) }
    fun onEventSheetDismiss() { _uiState.value = _uiState.value.copy(showEventSheet = false) }
    fun onErrorDismissed()    { _uiState.value = _uiState.value.copy(error = null) }
    fun onActionSuccessHandled() { _uiState.value = _uiState.value.copy(actionSuccess = null) }
}