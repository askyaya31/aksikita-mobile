package com.example.prototypevolunteerapp.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.SavedEventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedActivitiesUiState(
    val savedEvents: List<SavedEventDto> = emptyList(),
    val isLoading:   Boolean             = false,
    val errorMessage: String?            = null,
    val toastMessage: String?            = null
)

@HiltViewModel
class SavedActivitiesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedActivitiesUiState())
    val uiState: StateFlow<SavedActivitiesUiState> = _uiState.asStateFlow()

    init {
        loadSavedEvents()
    }

    fun loadSavedEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val resp = apiService.getSavedEvents()
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        savedEvents = resp.body()?.data ?: emptyList(),
                        isLoading   = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = "Gagal memuat saved (${resp.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = "Koneksi gagal: ${e.localizedMessage}"
                )
            }
        }
    }

    fun unsave(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.toggleSaveEvent(eventId)
                if (resp.isSuccessful) {
                    val isSaved = resp.body()?.saved ?: false
                    val msg     = if (isSaved) "Tersimpan!" else "Dihapus dari simpanan"
                    _uiState.value = _uiState.value.copy(
                        savedEvents  = _uiState.value.savedEvents.filter { it.event_id != eventId },
                        toastMessage = msg
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}