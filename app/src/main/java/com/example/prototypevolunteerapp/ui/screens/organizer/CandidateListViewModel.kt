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

    fun loadEvents(initialEventId: Int? = null, initialFilter: String = "Semua") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resp = apiService.getOrgEvents(status = null)

                if (!resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Gagal memuat kegiatan (${resp.code()})"
                    )
                    return@launch
                }

                val events = resp.body()?.data ?: emptyList()

                // LOGIKA BARU: Penanganan sinyal -1 dari Dashboard
                val startEvent = when (initialEventId) {
                    -1   -> null // Sinyal pasti untuk "Semua Kegiatan"
                    null -> events.firstOrNull() // Jika user manual buka tab Candidate
                    else -> events.find { it.id == initialEventId } // Jika buka kegiatan spesifik
                }

                _uiState.value = _uiState.value.copy(events = events, isLoading = false)

                // Teruskan initialFilter saat load data pendaftar
                loadRegistrations(startEvent, initialFilter)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun loadRegistrations(event: EventDto?, initialFilter: String = "Semua") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, selectedEvent = event)
            try {
                val combinedRegistrations = mutableListOf<RegistrationDto>()

                if (event != null) {
                    val resp = apiService.getEventRegistrations(eventId = event.id)
                    if (resp.isSuccessful) {
                        combinedRegistrations.addAll(resp.body()?.data ?: emptyList())
                    }
                } else {
                    val allEvents = _uiState.value.events
                    allEvents.forEach { e ->
                        val resp = apiService.getEventRegistrations(eventId = e.id)
                        if (resp.isSuccessful) {
                            combinedRegistrations.addAll(resp.body()?.data ?: emptyList())
                        }
                    }
                }

                val currentFilter = if (_uiState.value.selectedFilter == "Semua") initialFilter else _uiState.value.selectedFilter

                _uiState.value = _uiState.value.copy(
                    allRegistrations      = combinedRegistrations,
                    filteredRegistrations = applyFilter(combinedRegistrations, currentFilter),
                    isLoading             = false,
                    selectedFilter        = currentFilter
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Gagal memuat kandidat: ${e.localizedMessage}"
                )
            }
        }
    }

    fun onEventSelected(event: EventDto?) {
        _uiState.value = _uiState.value.copy(showEventSheet = false)
        loadRegistrations(event, "Semua") // Reset filter jika user ganti kegiatan manual
    }

    fun onFilterSelected(filter: String) {
        _uiState.value = _uiState.value.copy(
            selectedFilter        = filter,
            filteredRegistrations = applyFilter(_uiState.value.allRegistrations, filter)
        )
    }

    private fun applyFilter(list: List<RegistrationDto>, filter: String): List<RegistrationDto> {
        return when (filter) {
            "Pending"  -> list.filter { it.status == "pending" }
            "Diterima" -> list.filter { it.status == "confirmed" }
            "Ditolak"  -> list.filter { it.status == "cancelled" }
            "Hadir"    -> list.filter { it.status == "attended" }
            else       -> list  // "Semua"
        }
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
        val event = _uiState.value.selectedEvent
        val currentFilter = _uiState.value.selectedFilter
        loadRegistrations(event, currentFilter)
    }

    fun onEventSheetShow()       { _uiState.value = _uiState.value.copy(showEventSheet = true) }
    fun onEventSheetDismiss()    { _uiState.value = _uiState.value.copy(showEventSheet = false) }
    fun onErrorDismissed()       { _uiState.value = _uiState.value.copy(error = null) }
    fun onActionSuccessHandled() { _uiState.value = _uiState.value.copy(actionSuccess = null) }
}