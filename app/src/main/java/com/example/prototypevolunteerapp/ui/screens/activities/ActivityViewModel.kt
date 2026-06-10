package com.example.prototypevolunteerapp.ui.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.CategoryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ActivitiesUiState(
    val activities:       List<ActivityData> = emptyList(),
    val isLoading:        Boolean            = true,
    val isLoadingMore:    Boolean            = false,
    val errorMessage:     String?            = null,
    val categories:       List<CategoryDto> = emptyList(),
    val selectedCategory: CategoryDto?      = null,
    val selectedCity:     String            = "",
    val searchQuery:      String            = "",
    val likedIds:         Set<Int>          = emptySet(),
    val savedIds:         Set<Int>          = emptySet(),
    val currentPage:      Int               = 1,
    val lastPage:         Int               = 1,
    val toastMessage:     String?           = null
)

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    init {
        loadInitial()
    }

    private fun loadInitial() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val eventsDeferred = async { apiService.getPublicEvents(page = 1) }
                val categoriesDeferred = async { apiService.getCategories() }
                val likedDeferred = async {
                    try { apiService.getLikedEvents() } catch (e: Exception) {
                        android.util.Log.e("ActivitiesVM", "liked error: ${e.message}")
                        null
                    }
                }
                val savedDeferred = async {
                    try { apiService.getSavedEvents() } catch (e: Exception) {
                        android.util.Log.e("ActivitiesVM", "saved error: ${e.message}")
                        null
                    }
                }
                val eventsResp = eventsDeferred.await()
                android.util.Log.d("ActivitiesVM", "isSuccessful: ${eventsResp.isSuccessful}, code: ${eventsResp.code()}, dataSize: ${eventsResp.body()?.data?.size}")

                val categoriesResp = categoriesDeferred.await()
                android.util.Log.d("ActivitiesVM", "categories code: ${categoriesResp.code()}")

                val likedResp = likedDeferred.await()
                val savedResp = savedDeferred.await()

                android.util.Log.d("ActivitiesVM", "liked: ${likedResp?.code()}, saved: ${savedResp?.code()}")

                val likedIds = likedResp?.body()?.data?.map { it.event_id }?.toSet() ?: emptySet()
                val savedIds = savedResp?.body()?.data?.map { it.event_id }?.toSet() ?: emptySet()
                val categories = categoriesResp.body()?.categories ?: emptyList()

                if (eventsResp.isSuccessful && eventsResp.body() != null) {
                    val body = eventsResp.body()!!
                    _uiState.value = _uiState.value.copy(
                        activities   = mapEvents(body.data),
                        categories   = categories,
                        likedIds     = likedIds,
                        savedIds     = savedIds,
                        currentPage  = body.current_page,
                        lastPage     = body.last_page,
                        isLoading    = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading    = false,
                        errorMessage = "Gagal memuat kegiatan (${eventsResp.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading    = false,
                    errorMessage = "Tidak dapat terhubung ke server."
                )

            }
        }
    }

    fun loadActivities(resetPage: Boolean = true) {
        viewModelScope.launch {
            if (resetPage) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoadingMore = true)
            }

            val page = if (resetPage) 1 else _uiState.value.currentPage + 1

            try {
                val response = apiService.getPublicEvents(
                    search   = _uiState.value.searchQuery.ifBlank { null },
                    city     = _uiState.value.selectedCity.ifBlank { null },
                    category = _uiState.value.selectedCategory?.slug,
                    page     = page
                )

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    val mapped = mapEvents(body.data)
                    _uiState.value = _uiState.value.copy(
                        activities   = if (resetPage) mapped else _uiState.value.activities + mapped,
                        currentPage  = body.current_page,
                        lastPage     = body.last_page,
                        isLoading    = false,
                        isLoadingMore = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading     = false,
                        isLoadingMore = false,
                        errorMessage  = "Gagal memuat kegiatan (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading     = false,
                    isLoadingMore = false,
                    errorMessage  = "Tidak dapat terhubung ke server."
                )
            }
        }
    }

    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || state.currentPage >= state.lastPage) return
        loadActivities(resetPage = false)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadActivities(resetPage = true)
    }

    fun onCategorySelected(category: CategoryDto?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        loadActivities(resetPage = true)
    }

    fun onCityChanged(city: String) {
        _uiState.value = _uiState.value.copy(selectedCity = city)
        loadActivities(resetPage = true)
    }

    fun onClearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            selectedCity     = "",
            searchQuery      = ""
        )
        loadActivities(resetPage = true)
    }

    fun toggleLike(eventId: Int) {
        val current = _uiState.value.likedIds
        val isLiked = current.contains(eventId)
        // Optimistic update
        _uiState.value = _uiState.value.copy(
            likedIds = if (isLiked) current - eventId else current + eventId
        )
        viewModelScope.launch {
            try {
                val resp = apiService.toggleLikeEvent(eventId)
                if (!resp.isSuccessful) {
                    // Revert kalau gagal
                    _uiState.value = _uiState.value.copy(
                        likedIds = if (isLiked) current + eventId else current - eventId
                    )
                }
            } catch (e: Exception) {
                // Revert kalau gagal
                _uiState.value = _uiState.value.copy(
                    likedIds = if (isLiked) current + eventId else current - eventId
                )
            }
        }
    }

    fun toggleSave(eventId: Int) {
        val current = _uiState.value.savedIds
        val isSaved = current.contains(eventId)
        // Optimistic update
        _uiState.value = _uiState.value.copy(
            savedIds = if (isSaved) current - eventId else current + eventId
        )
        viewModelScope.launch {
            try {
                val resp = apiService.toggleSaveEvent(eventId)
                if (!resp.isSuccessful) {
                    // Revert kalau gagal
                    _uiState.value = _uiState.value.copy(
                        savedIds = if (isSaved) current + eventId else current - eventId
                    )
                }
            } catch (e: Exception) {
                // Revert kalau gagal
                _uiState.value = _uiState.value.copy(
                    savedIds = if (isSaved) current + eventId else current - eventId
                )
            }
        }
    }

    fun onToastShown() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }

    private fun mapEvents(events: List<com.example.prototypevolunteerapp.data.remote.dto.EventDto>): List<ActivityData> {
        return events.map { event ->
            ActivityData(
                id               = event.id.toString(),
                slug             = event.slug ?: "",
                title            = event.title,
                location         = buildString {
                    if (!event.location_name.isNullOrBlank()) append(event.location_name)
                    if (!event.city.isNullOrBlank()) {
                        if (isNotEmpty()) append(", ")
                        append(event.city)
                    }
                }.ifBlank { "Lokasi tidak tersedia" },
                description      = event.description ?: "",
                imageRes         = event.poster ?: "",
                organizationName = event.organization?.organization_name,
                startDate        = event.start_date,
                duration         = if (event.start_time != null && event.end_time != null)
                    "${event.start_time} - ${event.end_time}"
                else null,
                remainingQuota   = event.remaining_quota,
                category         = event.categories?.firstOrNull()?.name
            )
        }
    }
}
