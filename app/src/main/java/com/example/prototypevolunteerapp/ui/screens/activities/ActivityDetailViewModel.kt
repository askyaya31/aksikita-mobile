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
    val isSaved:         Boolean   = false
)

@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val apiService:       ApiService,
    private val userSession:      UserSession,
    private val savedStore:       SavedActivitiesStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()
    private val _isSaved  = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _isLiked  = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likesCount = MutableStateFlow(0)
    val likesCount: StateFlow<Int> = _likesCount.asStateFlow()
    private var currentEventId: Int = 0

    fun init(eventId: Int, slug: String, titleHint: String, descHint: String) {
        currentEventId = eventId
        _uiState.value = _uiState.value.copy(
            isLoggedIn = userSession.isLoggedIn
        )

        viewModelScope.launch {
            savedStore.savedIdsFlow.collect { savedIds ->
                _uiState.value = _uiState.value.copy(isSaved = savedIds.contains(eventId.toString()))
            }
        }

        if (eventId > 0) {
            fetchEventDetail(eventId, slug)
        }
    }
    fun toggleSave(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.toggleSaveEvent(eventId)
                if (resp.isSuccessful) {
                    _isSaved.value = resp.body()?.saved ?: false
                }
            } catch (_: Exception) {}
        }
    }

    fun toggleLike(eventId: Int) {
        viewModelScope.launch {
            try {
                val resp = apiService.toggleLikeEvent(eventId)
                if (resp.isSuccessful) {
                    _isLiked.value   = resp.body()?.liked ?: false
                    _likesCount.value = resp.body()?.likes_count ?: 0
                }
            } catch (_: Exception) {}
        }
    }
    fun onToggleSaved() {
        viewModelScope.launch {
            try {
                val resp = apiService.toggleSaveEvent(currentEventId)
                if (resp.isSuccessful) {
                    val isSaved = resp.body()?.saved ?: false
                    _uiState.value = _uiState.value.copy(isSaved = isSaved)
                }
            } catch (e: Exception) {
                savedStore.toggleSaved(currentEventId.toString())
            }
        }
    }

    private fun fetchEventDetail(eventId: Int, slug: String) {
        viewModelScope.launch {
            try {
                if (userSession.isLoggedIn && slug.isNotBlank()) {

                    val response = apiService.getVolunteerEventDetail(slug)
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = _uiState.value.copy(event = response.body()!!.event)
                        checkRegistrationStatus(eventId)
                        return@launch
                    }
                }

                if (slug.isNotBlank()) {
                    val response = apiService.getPublicEventDetail(slug)
                    if (response.isSuccessful && response.body() != null) {
                        _uiState.value = _uiState.value.copy(event = response.body()!!.event)
                        return@launch
                    }
                }

                android.util.Log.w(
                    "ActivityDetailVM",
                    "Tidak dapat fetch detail event id=$eventId slug=$slug"
                )

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
                    val registrations = response.body()!!.data
                    val alreadyRegistered = registrations.any { reg ->
                        reg.event?.id == eventId &&
                                reg.status !in listOf("cancelled")
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
                    val currentSlug = _uiState.value.event?.slug ?: ""
                    fetchEventDetail(currentEventId, currentSlug)

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
                    val currentSlug = _uiState.value.event?.slug ?: ""
                    fetchEventDetail(currentEventId, currentSlug)

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