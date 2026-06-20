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
data class OrgScheduleUiState(
    val events:    List<EventDto> = emptyList(),
    val isLoading: Boolean        = false,
    val error:     String?        = null
)

@HiltViewModel
class OrgScheduleViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrgScheduleUiState())
    val uiState: StateFlow<OrgScheduleUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val allEvents = mutableListOf<EventDto>()
                var page = 1
                var hasMore = true

                while (hasMore) {
                    val resp = apiService.getOrgEvents(status = null, page = page)
                    when {
                        resp.isSuccessful -> {
                            val body = resp.body()
                            allEvents.addAll(body?.data ?: emptyList())
                            hasMore = body != null && body.current_page < body.last_page
                            page++
                        }
                        resp.code() == 401 -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error     = "Sesi habis, silakan login ulang (401)"
                            )
                            return@launch
                        }
                        resp.code() == 403 -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error     = "Tidak punya akses ke endpoint ini (403)"
                            )
                            return@launch
                        }
                        else -> {
                            val errBody = resp.errorBody()?.string() ?: "no body"
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error     = "Error ${resp.code()}: $errBody"
                            )
                            return@launch
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(
                    events    = allEvents,
                    isLoading = false
                )
                allEvents.forEach { event ->
                    android.util.Log.d("ORG_SCHEDULE", "Event: ${event.title} | start_date: ${event.start_date}")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error     = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }
    fun retry() = loadEvents()
}