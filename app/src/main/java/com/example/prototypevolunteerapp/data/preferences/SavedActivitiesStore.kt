package com.example.prototypevolunteerapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedActivitiesStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val Context.savedDataStore: DataStore<Preferences>
                by preferencesDataStore(name = "saved_activities")

        private val SAVED_IDS_KEY = stringSetPreferencesKey("saved_event_ids")
    }

    val savedIdsFlow: Flow<Set<String>> = context.savedDataStore.data
        .map { prefs -> prefs[SAVED_IDS_KEY] ?: emptySet() }

    suspend fun toggleSaved(eventId: String) {
        context.savedDataStore.edit { prefs ->
            val current = prefs[SAVED_IDS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(eventId)) {
                current.remove(eventId)
            } else {
                current.add(eventId)
            }
            prefs[SAVED_IDS_KEY] = current
        }
    }

    suspend fun isEventSaved(eventId: String): Boolean {
        var result = false
        context.savedDataStore.edit { prefs ->
            result = prefs[SAVED_IDS_KEY]?.contains(eventId) == true
        }
        return result
    }

    suspend fun clearAll() {
        context.savedDataStore.edit { prefs ->
            prefs.remove(SAVED_IDS_KEY)
        }
    }
}