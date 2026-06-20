package com.example.prototypevolunteerapp.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.core.UserSession
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.model.NotificationRepository
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.prototypevolunteerapp.data.model.toActivityData
import javax.inject.Inject

data class HomeUiState(
    val userName:  String  = "Volunteer",
    val avatarUrl: String? = null,
    val selectedTab: Int = 0,
    val unreadNotifCount: Int = 0,
    val activeRegistrations: List<RegistrationDto> = emptyList(),
    val completedCount:      Int                   = 0,
    val apiEvents:    List<ActivityData> = emptyList(),
    val isLoadingApi: Boolean            = false,
    val apiError:     String?            = null,
    val categoryChips:    List<String> = emptyList(),
    val selectedCategory: String?      = null,
    val nearbyEvents:    List<ActivityData> = emptyList(),
    val isLoadingNearby: Boolean            = false,
    val userCity:        String?            = null,
    val upcomingSchedule: List<com.example.prototypevolunteerapp.data.remote.dto.ScheduleEventDto> = emptyList(),
    val recommendations:  List<ActivityData> = emptyList()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val apiService:             ApiService,
    private val userSession:            UserSession,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(
            userName  = userSession.currentUser?.name ?: "Volunteer",
            avatarUrl = userSession.currentUser?.avatarUrl
        )
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    val currentUser get() = userSession.currentUser

    init {
        viewModelScope.launch {
            try {
                val resp = apiService.getVolunteerUnreadCount()
                if (resp.isSuccessful) {
                    _uiState.update { it.copy(unreadNotifCount = resp.body()?.unread_count ?: 0) }
                } else {
                    notificationRepository.notifications.collect { list ->
                        _uiState.update { it.copy(unreadNotifCount = list.count { n -> !n.isRead }) }
                    }
                }
            } catch (e: Exception) {
                notificationRepository.notifications.collect { list ->
                    _uiState.update { it.copy(unreadNotifCount = list.count { n -> !n.isRead }) }
                }
            }
        }

        loadAll()
    }

    fun refreshUser() {
        _uiState.update { current ->
            current.copy(
                userName  = userSession.currentUser?.name ?: current.userName,
                avatarUrl = userSession.currentUser?.avatarUrl ?: current.avatarUrl
            )
        }
    }

    fun loadAll() {
        loadEvents()
        loadCategories()
        if (userSession.isLoggedIn) {
            loadRegistrations()
            fetchProfileThenNearby()
            loadUpcomingSchedule()
            loadRecommendations()
        }
    }

    private fun loadUpcomingSchedule() {
        viewModelScope.launch {
            runCatching { apiService.getSchedule(
                month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
                year  = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
            ) }
                .onSuccess { resp ->
                    _uiState.update { it.copy(upcomingSchedule = resp.upcoming.take(2)) }
                }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            runCatching { apiService.getRecommendations() }
                .onSuccess { list ->
                    _uiState.update { it.copy(
                        recommendations = list.map { dto -> dto.toActivityData() }
                    ) }
                }
        }
    }
    /**
     * Fetch profil volunteer langsung dari API untuk mendapatkan city,
     * lalu panggil loadNearbyEvents. Ini menghindari race condition di mana
     * city belum tersedia di UserSession saat HomeViewModel dibuat.
     */
    private fun fetchProfileThenNearby() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNearby = true) }
            try {
                val response = apiService.getVolunteerProfile()
                if (response.isSuccessful && response.body() != null) {
                    val vp = response.body()!!.user.volunteer_profile
                    val city = vp?.city?.trim()?.takeIf { it.isNotBlank() }

                    if (vp != null) userSession.updateVolunteerProfileDto(vp)

                    if (city != null) {
                        loadNearbyEvents(city)
                    } else {
                        android.util.Log.w("HomeViewModel", "Profil volunteer tidak memiliki city")
                        _uiState.update { it.copy(isLoadingNearby = false) }
                    }
                } else {
                    val city = userSession.volunteerProfileDto?.city?.trim()?.takeIf { it.isNotBlank() }
                        ?: userSession.currentVolunteerProfile?.birthPlace?.trim()?.takeIf { it.isNotBlank() }
                    if (city != null) {
                        loadNearbyEvents(city)
                    } else {
                        android.util.Log.w("HomeViewModel", "fetchProfile gagal dan session tidak punya city")
                        _uiState.update { it.copy(isLoadingNearby = false) }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("HomeViewModel", "fetchProfileThenNearby error: ${e.message}")
                val city = userSession.volunteerProfileDto?.city?.trim()?.takeIf { it.isNotBlank() }
                    ?: userSession.currentVolunteerProfile?.birthPlace?.trim()?.takeIf { it.isNotBlank() }
                if (city != null) loadNearbyEvents(city)
                else _uiState.update { it.copy(isLoadingNearby = false) }
            }
        }
    }

    private fun loadNearbyEvents(city: String) {
        viewModelScope.launch {
            android.util.Log.d("HomeViewModel", "loadNearbyEvents: mencari kegiatan di '$city'")
            _uiState.update { it.copy(userCity = city, isLoadingNearby = true) }
            try {
                val response = apiService.getPublicEvents(city = city)
                android.util.Log.d("HomeViewModel", "loadNearbyEvents response: ${response.code()}")
                if (response.isSuccessful && response.body() != null) {
                    val mapped = response.body()!!.data.map { event ->
                        ActivityData(
                            id          = event.id.toString(),
                            slug        = event.slug ?: "",
                            title       = event.title,
                            location    = buildString {
                                if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                if (!event.city.isNullOrBlank()) {
                                    if (isNotEmpty()) append(", ")
                                    append(event.city)
                                }
                            }.ifBlank { "Lokasi tidak tersedia" },
                            description = event.description ?: "",
                            imageRes    = event.poster ?: ""
                        )
                    }
                    android.util.Log.d("HomeViewModel", "loadNearbyEvents: ${mapped.size} kegiatan ditemukan di '$city'")
                    _uiState.update { it.copy(nearbyEvents = mapped, isLoadingNearby = false) }
                } else {
                    android.util.Log.w("HomeViewModel", "loadNearbyEvents: response ${response.code()} untuk city='$city'")
                    _uiState.update { it.copy(isLoadingNearby = false) }
                }
            } catch (e: Exception) {
                android.util.Log.w("HomeViewModel", "loadNearbyEvents error: ${e.message}")
                _uiState.update { it.copy(isLoadingNearby = false) }
            }
        }
    }

    private fun loadEvents(category: String? = _uiState.value.selectedCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApi = true, apiError = null) }
            try {
                val response = apiService.getPublicEvents(category = category)
                if (response.isSuccessful && response.body() != null) {
                    val mapped = response.body()!!.data.map { event ->
                        ActivityData(
                            id          = event.id.toString(),
                            slug        = event.slug ?: "",
                            title       = event.title,
                            location    = buildString {
                                if (!event.location_name.isNullOrBlank()) append(event.location_name)
                                if (!event.city.isNullOrBlank()) {
                                    if (isNotEmpty()) append(", ")
                                    append(event.city)
                                }
                            }.ifBlank { "Lokasi tidak tersedia" },
                            description = event.description ?: "",
                            imageRes    = event.poster ?: "",
                            organizationName = event.organization?.organization_name,
                            startDate = event.start_date,
                            duration = if (event.start_time != null && event.end_time != null)
                                "${event.start_time} - ${event.end_time}"
                            else null,
                            remainingQuota = event.remaining_quota,
                            category = event.categories?.firstOrNull()?.name
                        )
                    }
                    _uiState.update { it.copy(apiEvents = mapped, isLoadingApi = false) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoadingApi = false,
                            apiError     = "Gagal memuat kegiatan (${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "loadEvents error: ${e.message}")
                _uiState.update { it.copy(isLoadingApi = false, apiError = "Tidak dapat terhubung ke server") }
            }
        }
    }

    private fun loadRegistrations() {
        viewModelScope.launch {
            try {
                val response = apiService.getVolunteerRegistrations()
                if (response.isSuccessful && response.body() != null) {
                    val all = response.body()!!.data
                    val active    = all.filter { it.status in listOf("confirmed", "pending") }
                    val completed = all.count  { it.status in listOf("attended", "cancelled") }
                    _uiState.update {
                        it.copy(
                            activeRegistrations = active,
                            completedCount      = completed
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.w("HomeViewModel", "loadRegistrations error: ${e.message}")
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repeat(3) { attempt ->
                try {
                    val response = apiService.getCategories()
                    if (response.isSuccessful && response.body() != null) {
                        val names = response.body()!!.categories.map { it.name }
                        _uiState.update { it.copy(categoryChips = names) }
                        return@launch
                    }
                } catch (e: Exception) {
                    android.util.Log.w("HomeViewModel", "loadCategories attempt ${attempt + 1} error: ${e.message}")
                    if (attempt < 2) kotlinx.coroutines.delay(1000L)
                }
            }
        }
    }

    fun onTabSelected(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onCategoryChipSelected(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadEvents(category)
    }
}