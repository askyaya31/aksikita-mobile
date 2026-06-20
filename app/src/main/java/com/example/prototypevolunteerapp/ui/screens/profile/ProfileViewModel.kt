package com.example.prototypevolunteerapp.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.AppConfig
import com.example.prototypevolunteerapp.core.DateUtils
import com.example.prototypevolunteerapp.core.OrganizerSession
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.model.NotificationRepository
import com.example.prototypevolunteerapp.data.model.VolunteerDataStore
import com.example.prototypevolunteerapp.data.preferences.SessionPreferences
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val userName:    String  = "",
    val userEmail:   String  = "",
    val userPhone:   String? = null,
    val avatarUrl:   String? = null,

    val city:              String?       = null,
    val province:          String?       = null,
    val dateOfBirth:       String?       = null,
    val gender:            String?       = null,
    val bio:               String?       = null,
    val skills:            List<String>  = emptyList(),
    val interests:         List<String>  = emptyList(),
    val isLoggedIn:         Boolean = false,
    val shouldNavigateBack: Boolean = false,
    val isLogoutAction:     Boolean = false,
    val isLoading:          Boolean = false,
    val errorMessage:       String? = null,
    val totalEventsJoined:    Int                   = 0,
    val pendingRegistrations: Int                   = 0,
    val confirmedRegistrations: Int                 = 0,
    val recentRegistrations:  List<RegistrationDto> = emptyList(),
    val savedCount:           Int                   = 0,
    val likedCount:           Int                   = 0
) {
    val genderLabel: String? get() = when (gender) {
        "male"   -> "Laki-laki"
        "female" -> "Perempuan"
        "other"  -> "Lainnya"
        else     -> null
    }

    val locationLabel: String? get() {
        val parts = listOfNotNull(city, province).filter { it.isNotBlank() }
        return if (parts.isEmpty()) null else parts.joinToString(", ")
    }

    val hasProfile: Boolean get() =
        !bio.isNullOrBlank() || skills.isNotEmpty() || interests.isNotEmpty()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionPreferences:     SessionPreferences,
    private val volunteerDataStore:     VolunteerDataStore,
    private val userSession:            UserSession,
    private val organizerSession:       OrganizerSession,
    private val notificationRepository: NotificationRepository,
    private val apiService:             ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _unreadChatCount = MutableStateFlow(0)
    val unreadChatCount: StateFlow<Int> = _unreadChatCount.asStateFlow()
    init {
        loadProfile()
        loadUnreadChatCount()
    }
    private fun loadUnreadChatCount() {
        viewModelScope.launch {
            try {
                val response = apiService.getChatUnreadCount()
                if (response.isSuccessful) {
                    _unreadChatCount.value = response.body()?.unread_count ?: 0
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun loadProfile() {
        val user = userSession.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                shouldNavigateBack = true,
                isLogoutAction     = false
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val response = apiService.getVolunteerProfile()
                if (response.isSuccessful && response.body() != null) {
                    val serverUser = response.body()!!.user
                    val vp         = serverUser.volunteer_profile

                    val resolvedAvatar = resolveAvatarUrl(serverUser.avatar)
                        ?: resolveAvatarUrl(serverUser.volunteer_profile?.avatar)

                    userSession.updateAvatarUrl(resolvedAvatar)
                    sessionPreferences.updateAvatarUrl(resolvedAvatar)
                    if (vp != null) {
                        userSession.updateVolunteerProfileDto(vp)
                    }

                    _uiState.value = ProfileUiState(
                        userName           = serverUser.name,
                        userEmail          = serverUser.email,
                        userPhone          = serverUser.phone,
                        avatarUrl          = resolvedAvatar,
                        city               = vp?.city,
                        province           = vp?.province,
                        dateOfBirth        = DateUtils.formatDate(vp?.date_of_birth),
                        gender             = vp?.gender,
                        bio                = vp?.bio,
                        skills             = vp?.skills     ?: emptyList(),
                        interests          = vp?.interests  ?: emptyList(),
                        totalEventsJoined  = vp?.total_events_joined ?: 0,

                        isLoggedIn         = true,
                        isLoading          = false
                    )
                    return@launch
                }
            } catch (e: Exception) {
                android.util.Log.w("ProfileViewModel", "Gagal fetch profil: ${e.message}")
            }

            _uiState.value = ProfileUiState(
                userName     = user.name,
                userEmail    = user.email,
                isLoggedIn   = true,
                isLoading    = false,
                errorMessage = "Gagal memuat profil. Periksa koneksimu."
            )
        }
    }
    private fun resolveAvatarUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        return when {
            raw.startsWith("http") -> raw
                .replace("localhost", "10.0.2.2")
            else -> "${AppConfig.BASE_URL}storage/$raw"
                .replace("localhost", "10.0.2.2")
        }
    }

    fun loadActivityStats() {
        viewModelScope.launch {
            try {
                val regResp = apiService.getVolunteerRegistrations()
                if (regResp.isSuccessful) {
                    val regs = regResp.body()?.data ?: emptyList()
                    _uiState.value = _uiState.value.copy(
                        recentRegistrations    = regs.take(5),
                        pendingRegistrations   = regs.count { it.status == "pending" },
                        confirmedRegistrations = regs.count { it.status == "confirmed" }
                    )
                }
                val savedResp = apiService.getSavedEvents()
                if (savedResp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        savedCount = savedResp.body()?.data?.size ?: 0
                    )
                }
                val likedResp = apiService.getLikedEvents()
                if (likedResp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        likedCount = likedResp.body()?.data?.size ?: 0
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                apiService.logout()
            } catch (e: Exception) {
                android.util.Log.w("ProfileViewModel", "Server logout gagal: ${e.message}")
            }

            sessionPreferences.clearSession()
            volunteerDataStore.clearProfile()
            notificationRepository.clear()

            userSession.logout()
            organizerSession.logout()

            _uiState.value = _uiState.value.copy(
                isLoggedIn         = false,
                shouldNavigateBack = true,
                isLogoutAction     = true,
                isLoading          = false
            )
        }
    }

    fun onNavigateBackHandled() {
        _uiState.value = _uiState.value.copy(
            shouldNavigateBack = false,
            isLogoutAction     = false
        )
    }
}