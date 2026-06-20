package com.example.prototypevolunteerapp.ui.screens.schedule

import com.example.prototypevolunteerapp.data.remote.dto.ScheduleEventDto

data class ScheduleUiState(
    val upcoming: List<ScheduleEventDto> = emptyList(),
    val past: List<ScheduleEventDto> = emptyList(),
    val eventsByDate: Map<String, List<com.example.prototypevolunteerapp.data.remote.dto.ScheduleDateMarker>> = emptyMap(),
    val selectedMonth: Int = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1,
    val selectedYear: Int  = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR),
    val activeTab: ScheduleTab = ScheduleTab.UPCOMING,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class ScheduleTab { UPCOMING, PAST }