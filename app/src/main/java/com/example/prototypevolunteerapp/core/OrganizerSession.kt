package com.example.prototypevolunteerapp.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrganizerSession @Inject constructor() {
    data class Organizer(
        val name:    String,
        val email:   String,
        val logoUrl: String? = null
    )

    private val demoOrgs = mapOf(
        "aksisolosatu@gmail.com"       to Pair("aksisolosatu", "Aksi Solo Satu"),
        "peduly.solo@gmail.com"        to Pair("peduly123", "Peduly Solo"),
        "abdination.id@gmail.com"      to Pair("abdination", "Abdination Indonesia")
    )

    var currentOrg: Organizer? by mutableStateOf(null)
        private set

    val isLoggedIn get() = currentOrg != null

    fun login(email: String, password: String): Boolean {
        val entry = demoOrgs[email.trim().lowercase()]
        return if (entry != null && entry.first == password) {
            currentOrg = Organizer(name = entry.second, email = email.trim())
            true
        } else false
    }
    fun restoreSession(
        email:   String,
        name:    String,
        logoUrl: String? = null
    ) {
        currentOrg = Organizer(name = name, email = email, logoUrl = logoUrl)
    }

    fun updateLogoUrl(logoUrl: String?) {
        currentOrg = currentOrg?.copy(logoUrl = logoUrl)
    }

    fun logout() { currentOrg = null }
    fun hasAccess(): Boolean = isLoggedIn
}