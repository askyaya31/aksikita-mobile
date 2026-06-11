package com.example.prototypevolunteerapp.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.prototypevolunteerapp.data.model.getDummyVolunteers
import com.example.prototypevolunteerapp.data.model.Volunteer
import com.example.prototypevolunteerapp.data.model.VolunteerExp
import com.example.prototypevolunteerapp.data.remote.dto.VolunteerProfileDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSession @Inject constructor(){
    data class User(
        val name:      String,
        val email:     String,
        val avatarUrl: String? = null
    )

    private val demoAccounts = mapOf(
        "saskyaaliyaazizah06@gmail.com" to Pair("volunteer1", "Saskya Aliya"),
        "mumtatazah@gmail.com"          to Pair("volunteer2", "Mumtazah Nur Hidayati"),
        "sannytazkiyah@gmail.com"       to Pair("volunteer3", "Sanny Tazkiyah"),
        "queennikamutiara@gmail.com"    to Pair("volunteer4", "Queen Nika Prahara")
    )

    private val dummyVolunteerMap: MutableMap<String, Volunteer> = mutableMapOf(
        "saskyaaliyaazizah06@gmail.com" to getDummyVolunteers()[0],
        "mumtatazah@gmail.com"          to getDummyVolunteers()[1],
        "sannytazkiyah@gmail.com"       to getDummyVolunteers()[2],
        "queennikamutiara@gmail.com"    to getDummyVolunteers()[3],
    )

    var currentUser: User? by mutableStateOf(null)
        private set

    var currentVolunteerProfile: Volunteer? by mutableStateOf(null)
        private set

    val isLoggedIn get() = currentUser != null

    val isDummyAccount get() = currentUser?.email?.let { dummyVolunteerMap.containsKey(it) } ?: false

    var volunteerProfileDto: VolunteerProfileDto? by mutableStateOf(null)
        private set

    fun updateVolunteerProfileDto(profile: VolunteerProfileDto?) {
        volunteerProfileDto = profile
    }

    fun updateAvatarUrl(avatarUrl: String?) {
        currentUser = currentUser?.copy(avatarUrl = avatarUrl)
    }

    fun login(email: String, password: String): Boolean {
        val trimmed = email.trim().lowercase()
        val entry = demoAccounts[trimmed]
        return if (entry != null && entry.first == password) {
            currentUser = User(name = entry.second, email = trimmed)
            currentVolunteerProfile = dummyVolunteerMap[trimmed]
            true
        } else false
    }
    fun restoreSession(
        email:               String,
        name:                String,
        volunteer:           Volunteer?,
        avatarUrl:           String? = null,
        volunteerProfileDto: VolunteerProfileDto? = null
    ) {
        currentUser = User(name = name, email = email, avatarUrl = avatarUrl)
        
        if (volunteer != null) {
            currentVolunteerProfile = volunteer
        } else if (volunteerProfileDto != null) {
            currentVolunteerProfile = Volunteer(
                name        = name,
                birthPlace  = volunteerProfileDto.city ?: "",
                birthDate   = volunteerProfileDto.date_of_birth ?: "",
                education   = "",
                skills      = volunteerProfileDto.skills ?: emptyList(),
                interests   = volunteerProfileDto.interests ?: emptyList(),
                about       = volunteerProfileDto.bio ?: "",
                experiences = emptyList(),
                imageRes    = 0,
                email       = email,
                phone       = null
            )
        } else {
            currentVolunteerProfile = dummyVolunteerMap[email]
        }
        email:     String,
        name:      String,
        volunteer: Volunteer?,
        avatarUrl: String? = null,
        volunteerProfileDto: VolunteerProfileDto? = null
    ) {
        currentUser = User(name = name, email = email, avatarUrl = avatarUrl)
        currentVolunteerProfile = volunteer ?: dummyVolunteerMap[email]
        this.volunteerProfileDto = volunteerProfileDto
    }

    fun logout() {
        currentUser = null
        currentVolunteerProfile = null
    }
}