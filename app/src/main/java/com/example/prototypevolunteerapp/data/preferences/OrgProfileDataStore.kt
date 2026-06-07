package com.example.prototypevolunteerapp.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

private val Context.orgProfileDataStore by preferencesDataStore(name = "org_profile_store")

@Singleton
class OrgProfileDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private fun keyFor(email: String) = stringPreferencesKey("org_profile:$email")
    }

    fun getProfileFlow(email: String): Flow<Map<String, String>> =
        context.orgProfileDataStore.data.map { prefs ->
            val json = prefs[keyFor(email)] ?: return@map emptyMap()
            parseJson(json)
        }

    suspend fun saveProfile(
        email:       String,
        name:        String,
        description: String,
        instagram:   String,
        website:     String,
        portfolio:   String
    ) {
        val json = JSONObject().apply {
            put("name",        name)
            put("description", description)
            put("instagram",   instagram)
            put("website",     website)
            put("portfolio",   portfolio)
        }.toString()

        context.orgProfileDataStore.edit { prefs ->
            prefs[keyFor(email)] = json
        }
    }
    suspend fun clearProfile(email: String) {
        context.orgProfileDataStore.edit { prefs ->
            prefs.remove(keyFor(email))
        }
    }

    private fun parseJson(json: String): Map<String, String> = try {
        val obj = JSONObject(json)
        mapOf(
            "name"        to obj.optString("name",        ""),
            "description" to obj.optString("description", ""),
            "instagram"   to obj.optString("instagram",   ""),
            "website"     to obj.optString("website",     ""),
            "portfolio"   to obj.optString("portfolio",   "[]")
        )
    } catch (e: Exception) {
        emptyMap()
    }
}
