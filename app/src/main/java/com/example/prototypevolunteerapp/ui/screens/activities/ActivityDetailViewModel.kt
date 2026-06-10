package com.example.prototypevolunteerapp.ui.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.preferences.SavedActivitiesStore
import com.example.prototypevolunteerapp.data.remote.dto.EventDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivityDetailUiState(
    val event:           EventDto? = null,
    val isLoggedIn:      Boolean   = false,
    val isRegistered:    Boolean   = false,
    val isProcessing:    Boolean   = false,
    val feedbackMessage: String?   = null,
    val isSaved:         Boolean   = false,
    val isLiked:         Boolean   = false,
    val likesCount:      Int       = 0
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val apiService:  ApiService,
    private val userSession: UserSession,
    private val savedStore:  SavedActivitiesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    private var currentEventId: Int = 0

    fun init(eventId: Int, slug: String, titleHint: String, descHint: String) {
        currentEventId = eventId
        _uiState.value = _uiState.value.copy(isLoggedIn = userSession.isLoggedIn)

        if (eventId > 0) {
            fetchEventDetail(eventId, slug)
            if (userSession.isLoggedIn) {
                checkSavedStatus(eventId)
                checkLikedStatus(eventId)
            }
        }
    }

    private fun checkSavedStatus(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.getSavedEvents()
                if (resp.isSuccessful) {
                    val isSaved = resp.body()?.data?.any { it.event_id == eventId } ?: false
                    _uiState.value = _uiState.value.copy(isSaved = isSaved)
                }
            } catch (_: Exception) {}
        }
    }

    private fun checkLikedStatus(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.getLikedEvents()
                if (resp.isSuccessful) {
                    val liked = resp.body()?.data?.find { it.event_id == eventId }
                    _uiState.value = _uiState.value.copy(
                        isLiked    = liked != null,
                        likesCount = _uiState.value.event?.likes_count ?: 0
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleSave() {
        val current = _uiState.value.isSaved
        // Optimistic update
        _uiState.value = _uiState.value.copy(isSaved = !current)
        viewModelScope.launch {
            try {
                val resp = apiService.toggleSaveEvent(currentEventId)
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(isSaved = resp.body()?.saved ?: !current)
                } else {
                    // Revert
                    _uiState.value = _uiState.value.copy(isSaved = current)
                }
            } catch (_: Exception) {
                // Revert
                _uiState.value = _uiState.value.copy(isSaved = current)
            }
        }
    }

    fun toggleLike() {
        val current      = _uiState.value.isLiked
        val currentCount = _uiState.value.likesCount
        // Optimistic update
        _uiState.value = _uiState.value.copy(
            isLiked    = !current,
            likesCount = if (current) currentCount - 1 else currentCount + 1
        )
        viewModelScope.launch {
            try {
                val resp = apiService.toggleLikeEvent(currentEventId)
                if (resp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLiked    = resp.body()?.liked ?: !current,
                        likesCount = resp.body()?.likes_count ?: currentCount
                    )
                } else {
                    // Revert
                    _uiState.value = _uiState.value.copy(
                        isLiked    = current,
                        likesCount = currentCount
                    )
                }
            } catch (_: Exception) {
                // Revert
                _uiState.value = _uiState.value.copy(
                    isLiked    = current,
                    likesCount = currentCount
                )
            }
        }
    }

    private fun fetchEventDetail(eventId: Int, slug: String) {
        viewModelScope.launch {
            try {
                if (userSession.isLoggedIn && slug.isNotBlank()) {
                    val response = apiService.getVolunteerEventDetail(slug)
                    if (response.isSuccessful && response.body() != null) {
                        val event = response.body()!!.event
                        _uiState.value = _uiState.value.copy(
                            event      = event,
                            likesCount = event.likes_count ?: 0
                        )
                        checkRegistrationStatus(eventId)
                        return@launch
                    }
                }
                if (slug.isNotBlank()) {
                    val response = apiService.getPublicEventDetail(slug)
                    if (response.isSuccessful && response.body() != null) {
                        val event = response.body()!!.event
                        _uiState.value = _uiState.value.copy(
                            event      = event,
                            likesCount = event.likes_count ?: 0
                        )
                        return@launch
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ActivityDetailVM", "Error: ${e.message}")
            }
        }
    }

    private fun checkRegistrationStatus(eventId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getVolunteerRegistrations()
                if (response.isSuccessful && response.body() != null) {
                    val alreadyRegistered = response.body()!!.data.any { reg ->
                        reg.event?.id == eventId && reg.status !in listOf("cancelled")
                    }
                    _uiState.value = _uiState.value.copy(isRegistered = alreadyRegistered)
                }
            } catch (e: Exception) {
                android.util.Log.w("ActivityDetailVM", "Gagal cek status registrasi: ${e.message}")
            }
        }
    }

    fun onRegisterToEvent() {
        if (currentEventId <= 0 || _uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            try {
                val response = apiService.registerToEvent(currentEventId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isRegistered    = true,
                        isProcessing    = false,
                        feedbackMessage = "Pendaftaran berhasil! Selamat bergabung."
                    )
                    fetchEventDetail(currentEventId, _uiState.value.event?.slug ?: "")
                } else {
                    val errorMsg = when (response.code()) {
                        422  -> "Pendaftaran gagal: kuota penuh atau kamu sudah terdaftar."
                        401  -> "Sesi habis, silakan login ulang."
                        else -> "Pendaftaran gagal (${response.code()})."
                    }
                    _uiState.value = _uiState.value.copy(
                        isProcessing    = false,
                        feedbackMessage = errorMsg
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing    = false,
                    feedbackMessage = "Tidak dapat terhubung ke server."
                )
            }
        }
    }

    fun onCancelRegistration() {
        if (currentEventId <= 0 || _uiState.value.isProcessing) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true)
            try {
                val response = apiService.cancelRegistration(currentEventId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isRegistered    = false,
                        isProcessing    = false,
                        feedbackMessage = "Pendaftaran berhasil dibatalkan."
                    )
                    fetchEventDetail(currentEventId, _uiState.value.event?.slug ?: "")
                } else {
                    _uiState.value = _uiState.value.copy(
                        isProcessing    = false,
                        feedbackMessage = "Pembatalan gagal (${response.code()})."
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing    = false,
                    feedbackMessage = "Tidak dapat terhubung ke server."
                )
            }
        }
    }

    fun onFeedbackShown() {
        _uiState.value = _uiState.value.copy(feedbackMessage = null)
    }
}