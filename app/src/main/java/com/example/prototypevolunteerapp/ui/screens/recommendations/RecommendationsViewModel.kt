package com.example.prototypevolunteerapp.ui.screens.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.model.ActivityData
import com.example.prototypevolunteerapp.data.model.toActivityData
import com.example.prototypevolunteerapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationsUiState(
    val items: List<ActivityData> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(RecommendationsUiState())
    val state = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true, error = null) }
        runCatching { api.getRecommendations() }
            .onSuccess { list ->
                _state.update { state ->
                    state.copy(
                        items     = list.map { dto -> dto.toActivityData() },
                        isLoading = false
                    )
                }
            }
            .onFailure { e ->
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
    }
}