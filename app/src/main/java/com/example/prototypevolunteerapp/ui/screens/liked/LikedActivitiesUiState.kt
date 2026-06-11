package com.example.prototypevolunteerapp.ui.screens.liked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.LikedEventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LikedActivitiesUiState(
    val likedEvents:  List<LikedEventDto> = emptyList(),
    val savedIds:     Set<Int>            = emptySet(),
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

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val likedDeferred = async { apiService.getLikedEvents() }
                val savedDeferred = async {
                    try { apiService.getSavedEvents() } catch (_: Exception) { null }
                }

                val likedResp = likedDeferred.await()
                val savedResp = savedDeferred.await()

                val savedIds = savedResp?.body()?.data?.map { it.event_id }?.toSet() ?: emptySet()

                if (likedResp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        likedEvents = likedResp.body()?.data ?: emptyList(),
                        savedIds    = savedIds,
                        isLoading   = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = "Gagal memuat (${likedResp.code()})"
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
                    _uiState.value = _uiState.value.copy(
                        likedEvents  = _uiState.value.likedEvents.filter { it.event_id != eventId },
                        toastMessage = "Dihapus dari suka"
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleSave(eventId: Int) {
        val current = _uiState.value.savedIds
        val isSaved = current.contains(eventId)
        _uiState.value = _uiState.value.copy(
            savedIds = if (isSaved) current - eventId else current + eventId
        )
        viewModelScope.launch {
            try {
                val resp = apiService.toggleSaveEvent(eventId)
                if (!resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        savedIds = if (isSaved) current + eventId else current - eventId
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    savedIds = if (isSaved) current + eventId else current - eventId
                )
            }
        }
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun loadLikedEvents() { loadAll() }
}