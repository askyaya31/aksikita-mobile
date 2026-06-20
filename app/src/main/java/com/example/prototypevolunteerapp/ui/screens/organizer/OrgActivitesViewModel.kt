package com.example.prototypevolunteerapp.ui.screens.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrgActivitiesUiState(
    val events:         List<EventDto> = emptyList(),
    val isLoading:      Boolean        = false,
    val error:          String?        = null,
    val searchQuery:    String         = "",
    val selectedStatus: String?        = null
) {
    val filteredEvents: List<EventDto>
        get() {
            var list = if (selectedStatus == null) events
            else events.filter { it.status == selectedStatus }
            if (searchQuery.isNotBlank()) {
                list = list.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.city?.contains(searchQuery, ignoreCase = true) == true
                }
            }
            return list
        }
}

@HiltViewModel
class OrgActivitiesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrgActivitiesUiState())
    val uiState: StateFlow<OrgActivitiesUiState> = _uiState.asStateFlow()

    fun load(initialStatus: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading      = true,
                error          = null,
                selectedStatus = initialStatus
            )
            try {
                val resp = apiService.getOrgEvents(status = null)
                if (resp.isSuccessful && resp.body() != null) {
                    _uiState.value = _uiState.value.copy(
                        events    = resp.body()!!.data,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = "Gagal memuat kegiatan (${resp.code()})"
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

    fun onCompleteEvent(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.completeOrgEvent(eventId)
                if (resp.isSuccessful) {
                    reload()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Gagal menyelesaikan kegiatan")
            }
        }
    }

    fun onCancelEventRequest(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.cancelOrgEvent(eventId, emptyMap())
                if (resp.isSuccessful) {
                    reload()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Gagal membatalkan kegiatan")
            }
        }
    }
    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onStatusSelected(status: String?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
    }
    fun reload() = load(_uiState.value.selectedStatus)
}