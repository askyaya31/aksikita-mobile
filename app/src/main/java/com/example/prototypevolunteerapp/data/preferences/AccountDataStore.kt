package com.example.prototypevolunteerapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

val Context.accountDataStore: DataStore<Preferences> by preferencesDataStore(name = "account_store")

@Singleton
class AccountDataStore @Inject constructor(
     @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_VOLUNTEER_ACCOUNTS  = stringPreferencesKey("volunteer_accounts")
        private val KEY_ORGANIZER_ACCOUNTS  = stringPreferencesKey("organizer_accounts")
    }

    data class RegisteredVolunteer(val email: String, val password: String, val name: String)
    data class RegisteredOrganizer(val email: String, val password: String, val orgName: String)
    val volunteerAccountsFlow: Flow<List<RegisteredVolunteer>> =
        context.accountDataStore.data.map { prefs ->
            parseVolunteers(prefs[KEY_VOLUNTEER_ACCOUNTS] ?: "[]")
        }

    suspend fun registerVolunteer(email: String, password: String, name: String) {
        context.accountDataStore.edit { prefs ->
            val existing = parseVolunteers(prefs[KEY_VOLUNTEER_ACCOUNTS] ?: "[]").toMutableList()
            existing.add(RegisteredVolunteer(email.trim().lowercase(), password, name.trim()))
            prefs[KEY_VOLUNTEER_ACCOUNTS] = serializeVolunteers(existing)
        }
    }

    suspend fun isVolunteerEmailTaken(email: String): Boolean {
        val accounts = volunteerAccountsFlow.first()
        return accounts.any { it.email == email.trim().lowercase() }
    }

    suspend fun findVolunteer(email: String, password: String): RegisteredVolunteer? {
        val accounts = volunteerAccountsFlow.first()
        return accounts.find {
            it.email == email.trim().lowercase() && it.password == password
        }
    }

    val organizerAccountsFlow: Flow<List<RegisteredOrganizer>> =
        context.accountDataStore.data.map { prefs ->
            parseOrganizers(prefs[KEY_ORGANIZER_ACCOUNTS] ?: "[]")
        }

    suspend fun registerOrganizer(email: String, password: String, orgName: String) {
        context.accountDataStore.edit { prefs ->
            val existing = parseOrganizers(prefs[KEY_ORGANIZER_ACCOUNTS] ?: "[]").toMutableList()
            existing.add(RegisteredOrganizer(email.trim().lowercase(), password, orgName.trim()))
            prefs[KEY_ORGANIZER_ACCOUNTS] = serializeOrganizers(existing)
        }
    }

    suspend fun isOrganizerEmailTaken(email: String): Boolean {
        val accounts = organizerAccountsFlow.first()
        return accounts.any { it.email == email.trim().lowercase() }
    }

    suspend fun findOrganizer(email: String, password: String): RegisteredOrganizer? {
        val accounts = organizerAccountsFlow.first()
        return accounts.find {
            it.email == email.trim().lowercase() && it.password == password
        }
    }

    private fun parseVolunteers(json: String): List<RegisteredVolunteer> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map {
            val obj = arr.getJSONObject(it)
            RegisteredVolunteer(
                email    = obj.getString("email"),
                password = obj.getString("password"),
                name     = obj.getString("name")
            )
        }
    } catch (e: Exception) { emptyList() }

    private fun serializeVolunteers(list: List<RegisteredVolunteer>): String {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("email", it.email)
                put("password", it.password)
                put("name", it.name)
            })
        }
        return arr.toString()
    }

    private fun parseOrganizers(json: String): List<RegisteredOrganizer> = try {
        val arr = JSONArray(json)
        (0 until arr.length()).map {
            val obj = arr.getJSONObject(it)
            RegisteredOrganizer(
                email    = obj.getString("email"),
                password = obj.getString("password"),
                orgName  = obj.getString("orgName")
            )
        }
    } catch (e: Exception) { emptyList() }

    private fun serializeOrganizers(list: List<RegisteredOrganizer>): String {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("email", it.email)
                put("password", it.password)
                put("orgName", it.orgName)
            })
        }
        return arr.toString()
    }
}
