package com.example.prototypevolunteerapp.ui.screens.volunteers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prototypevolunteerapp.data.model.Volunteer
import com.example.prototypevolunteerapp.data.model.getDummyVolunteers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VolunteersViewModel @Inject constructor() : ViewModel() {
    private val _volunteers = MutableStateFlow<List<Volunteer>>(emptyList())
    val volunteers: StateFlow<List<Volunteer>> = _volunteers.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init { loadVolunteers()}

    private fun loadVolunteers() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(800)
            _volunteers.value = getDummyVolunteers()
            _isLoading.value = false
        }
    }
}
