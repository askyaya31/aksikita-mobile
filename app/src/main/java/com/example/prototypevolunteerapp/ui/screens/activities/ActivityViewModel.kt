package com.example.prototypevolunteerapp.ui.screens.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.CategoryDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _activities = MutableStateFlow<List<ActivityData>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryDto?>(null)
    val selectedCategory: StateFlow<CategoryDto?> = _selectedCategory.asStateFlow()

    private val _selectedCity = MutableStateFlow("")
    val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _filteredActivities = MutableStateFlow<List<ActivityData>>(emptyList())
    val filteredActivities: StateFlow<List<ActivityData>> = _filteredActivities.asStateFlow()

    init {
        loadCategories()
        loadActivities()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val response = apiService.getCategories()
                if (response.isSuccessful) {
                    _categories.value = response.body()?.categories ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.w("ActivitiesViewModel", "Gagal load kategori: ${e.message}")
            }
        }
    }

    fun loadActivities(page: Int = 1) {
        viewModelScope.launch {
            _isLoading.value    = true
            _errorMessage.value = null

            try {
                val response = apiService.getPublicEvents(
                    search   = _searchQuery.value.ifBlank { null },
                    city     = _selectedCity.value.ifBlank { null },
                    category = _selectedCategory.value?.slug,
                    page     = page
                )

                if (response.isSuccessful && response.body() != null) {
                    val events = response.body()!!.data
                    val mapped = events.map { event ->
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
                            imageRes    = event.poster ?: "social_activity1",
                            instagram   = null,
                            link        = null
                        )
                    }
                    _activities.value         = mapped
                    _filteredActivities.value = applyLocalFilter(mapped)
                } else {
                    _errorMessage.value = "Gagal memuat kegiatan (${response.code()})."
                }
            } catch (e: Exception) {
                android.util.Log.e("ActivitiesViewModel", "Error fetch events: ${e.message}")
                _errorMessage.value = "Tidak dapat terhubung ke server. Periksa koneksi internet."
            }

            _isLoading.value = false
        }
    }
    fun onSearchQueryChange(query: String) {
        _searchQuery.value        = query
        _filteredActivities.value = applyLocalFilter(_activities.value)
    }

    fun onCategorySelected(category: CategoryDto?) {
        _selectedCategory.value = category
        loadActivities()
    }

    fun onCityChanged(city: String) {
        _selectedCity.value = city
        loadActivities()
    }

    fun onClearFilters() {
        _selectedCategory.value = null
        _selectedCity.value     = ""
        _searchQuery.value      = ""
        loadActivities()
    }

    private fun applyLocalFilter(list: List<ActivityData>): List<ActivityData> {
        val q = _searchQuery.value
        return if (q.isBlank()) list
        else list.filter {
            it.title.contains(q, ignoreCase = true) ||
                    it.location.contains(q, ignoreCase = true)
        }
    }
}