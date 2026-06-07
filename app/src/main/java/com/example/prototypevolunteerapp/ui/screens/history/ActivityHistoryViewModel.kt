package com.example.prototypevolunteerapp.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.remote.ApiService
import com.example.prototypevolunteerapp.data.remote.dto.RegistrationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val registrations: List<RegistrationDto> = emptyList(),
    val isLoading:     Boolean  = true,
    val errorMessage:  String?  = null,
    val isEmpty:       Boolean  = false
)

@HiltViewModel
class ActivityHistoryViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init { loadHistory() }

    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = HistoryUiState(isLoading = true, errorMessage = null)
            try {
                val response = apiService.getVolunteerRegistrations()
                if (response.isSuccessful) {
                    val list = response.body()?.data ?: emptyList()
                    _uiState.value = HistoryUiState(
                        registrations = list,
                        isLoading     = false,
                        isEmpty       = list.isEmpty()
                    )
                } else {
                    _uiState.value = HistoryUiState(
                        isLoading    = false,
                        errorMessage = "Gagal memuat riwayat (${response.code()})"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState(
                    isLoading    = false,
                    errorMessage = "Tidak dapat terhubung ke server. Periksa koneksimu."
                )
            }
        }
    }
}