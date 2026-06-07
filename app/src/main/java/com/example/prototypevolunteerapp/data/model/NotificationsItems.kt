package com.example.prototypevolunteerapp.data.model

data class NotificationItems(
    val id: Int,
    val organizationName: String,
    val organizationLogoRes: Int,
    val title: String,
    val message: String,
    val timestamp: String,
    val isRead: Boolean = false
)