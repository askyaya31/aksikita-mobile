package com.example.prototypevolunteerapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

@Singleton
class SessionPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_ROLE      = stringPreferencesKey("session_role")
        private val KEY_EMAIL     = stringPreferencesKey("session_email")
        private val KEY_NAME      = stringPreferencesKey("session_name")
        private val KEY_TOKEN     = stringPreferencesKey("auth_token")
        private val KEY_AVATAR    = stringPreferencesKey("session_avatar_url")
        private val KEY_LOGO      = stringPreferencesKey("session_logo_url")

        const val ROLE_VOLUNTEER = "volunteer"
        const val ROLE_ORGANIZER = "organization"
    }

    data class SavedSession(
        val role:      String,
        val email:     String,
        val name:      String,
        val avatarUrl: String? = null,
        val logoUrl:   String? = null
    )

    val savedSession: Flow<SavedSession?> = context.sessionDataStore.data
        .map { prefs ->
            val role  = prefs[KEY_ROLE]
            val email = prefs[KEY_EMAIL]
            val name  = prefs[KEY_NAME]
            if (!role.isNullOrBlank() && !email.isNullOrBlank() && !name.isNullOrBlank()) {
                SavedSession(
                    role      = role,
                    email     = email,
                    name      = name,
                    avatarUrl = prefs[KEY_AVATAR]?.ifBlank { null },
                    logoUrl   = prefs[KEY_LOGO]?.ifBlank { null }
                )
            } else null
        }

    suspend fun saveVolunteerSession(email: String, name: String, avatarUrl: String? = null) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ROLE]  = ROLE_VOLUNTEER
            prefs[KEY_EMAIL] = email
            prefs[KEY_NAME]  = name
            if (avatarUrl != null) prefs[KEY_AVATAR] = avatarUrl
            else prefs.remove(KEY_AVATAR)
        }
    }

    suspend fun saveOrganizerSession(email: String, name: String, logoUrl: String? = null) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_ROLE]  = ROLE_ORGANIZER
            prefs[KEY_EMAIL] = email
            prefs[KEY_NAME]  = name
            if (logoUrl != null) prefs[KEY_LOGO] = logoUrl
            else prefs.remove(KEY_LOGO)
        }
    }

    suspend fun updateAvatarUrl(avatarUrl: String?) {
        context.sessionDataStore.edit { prefs ->
            if (avatarUrl != null) prefs[KEY_AVATAR] = avatarUrl
            else prefs.remove(KEY_AVATAR)
        }
    }

    suspend fun updateLogoUrl(logoUrl: String?) {
        context.sessionDataStore.edit { prefs ->
            if (logoUrl != null) prefs[KEY_LOGO] = logoUrl
            else prefs.remove(KEY_LOGO)
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
        }
    }

    suspend fun getAuthToken(): String? {
        return context.sessionDataStore.data.map { prefs ->
            prefs[KEY_TOKEN]
        }.first()
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_NAME)
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_AVATAR)
            prefs.remove(KEY_LOGO)
        }
    }
}