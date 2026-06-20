package com.example.prototypevolunteerapp.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val api: ApiService
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state = _state.asStateFlow()

    init { loadSchedule() }

    fun loadSchedule() = viewModelScope.launch {
        val s = _state.value
        _state.update { it.copy(isLoading = true, error = null) }
        runCatching { api.getSchedule(s.selectedMonth, s.selectedYear) }
            .onSuccess { resp ->
                _state.update {
                    it.copy(
                        upcoming     = resp.upcoming,
                        past         = resp.past,
                        eventsByDate = resp.eventsByDate,
                        isLoading    = false
                    )
                }
            }
            .onFailure { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
    }

    fun changeMonth(delta: Int) {
        _state.update { s ->
            var m = s.selectedMonth + delta
            var y = s.selectedYear
            if (m > 12) { m = 1; y++ }
            if (m < 1)  { m = 12; y-- }
            s.copy(selectedMonth = m, selectedYear = y)
        }
        loadSchedule()
    }

    fun setTab(tab: ScheduleTab) = _state.update { it.copy(activeTab = tab) }
}