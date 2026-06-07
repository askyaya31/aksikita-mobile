package com.example.prototypevolunteerapp.ui.screens.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.LikedEventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikedActivitiesUiState(
    val likedEvents:  List<LikedEventDto> = emptyList(),
    val isLoading:    Boolean             = false,
    val errorMessage: String?             = null,
    val toastMessage: String?             = null
)

@HiltViewModel
class LikedActivitiesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(LikedActivitiesUiState())
    val uiState: StateFlow<LikedActivitiesUiState> = _uiState.asStateFlow()

    init {
        loadLikedEvents()
    }

    fun loadLikedEvents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val resp = apiService.getLikedEvents()
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        likedEvents = resp.body()?.data ?: emptyList(),
                        isLoading   = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = "Gagal memuat liked (${resp.code()})"
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

    fun unlike(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.toggleLikeEvent(eventId)
                if (resp.isSuccessful) {
                    val isLiked = resp.body()?.liked ?: false
                    val msg     = if (isLiked) "Disukai!" else "Dihapus dari suka"
                    _uiState.value = _uiState.value.copy(
                        likedEvents  = _uiState.value.likedEvents.filter { it.event_id != eventId },
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