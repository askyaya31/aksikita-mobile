package com.example.prototypevolunteerapp.data.model

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor() {
    private val _notifications = MutableStateFlow<List<NotificationItems>>(emptyList())
    val notifications: StateFlow<List<NotificationItems>> = _notifications.asStateFlow()

    fun initForUser(isDummyAccount: Boolean) {
        _notifications.value = if (isDummyAccount) getDummyNotifications() else emptyList()
    }

    fun clear() {
        _notifications.value = emptyList()
    }

    fun markAsRead(id: Int) {
        _notifications.update { list ->
            list.map { if (it.id == id) it.copy(isRead = true) else it }
        }
    }

    fun markAllAsRead() {
        _notifications.update { list ->
            list.map { it.copy(isRead = true) }
        }
    }
}