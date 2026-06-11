package com.example.prototypevolunteerapp.ui.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.SavedEventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SavedActivitiesUiState(
    val savedEvents:  List<SavedEventDto> = emptyList(),
    val likedIds:     Set<Int>            = emptySet(),
    val isLoading:    Boolean             = false,
    val errorMessage: String?             = null,
    val toastMessage: String?             = null
)

@HiltViewModel
class SavedActivitiesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SavedActivitiesUiState())
    val uiState: StateFlow<SavedActivitiesUiState> = _uiState.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val savedDeferred = async { apiService.getSavedEvents() }
                val likedDeferred = async {
                    try { apiService.getLikedEvents() } catch (_: Exception) { null }
                }

                val savedResp = savedDeferred.await()
                val likedResp = likedDeferred.await()

                val likedIds = likedResp?.body()?.data?.map { it.event_id }?.toSet() ?: emptySet()

                if (savedResp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        savedEvents = savedResp.body()?.data ?: emptyList(),
                        likedIds    = likedIds,
                        isLoading   = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = "Gagal memuat (${savedResp.code()})"
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
                    _uiState.value = _uiState.value.copy(
                        savedEvents  = _uiState.value.savedEvents.filter { it.event_id != eventId },
                        toastMessage = "Dihapus dari simpanan"
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleLike(eventId: Int) {
        val current = _uiState.value.likedIds
        val isLiked = current.contains(eventId)
        _uiState.value = _uiState.value.copy(
            likedIds = if (isLiked) current - eventId else current + eventId
        )
        viewModelScope.launch {
            try {
                val resp = apiService.toggleLikeEvent(eventId)
                if (!resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        likedIds = if (isLiked) current + eventId else current - eventId
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    likedIds = if (isLiked) current + eventId else current - eventId
                )
            }
        }
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    fun loadSavedEvents() { loadAll() }
}