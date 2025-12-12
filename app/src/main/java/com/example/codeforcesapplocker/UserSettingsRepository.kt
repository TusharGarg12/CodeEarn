package com.example.codeforcesapplocker

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Setup DataStore extension
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_settings")

@Singleton
class UserSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val USER_HANDLE_KEY = stringPreferencesKey("codeforces_handle")

    // CHANGED: Return Flow<String> (non-nullable)
    // If the key is missing (new install), it now defaults to "" instead of null.
    val userHandle: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[USER_HANDLE_KEY] ?: ""
        }

    // Save the handle
    suspend fun saveHandle(handle: String) {
        context.dataStore.edit { preferences ->
            preferences[USER_HANDLE_KEY] = handle
        }
    }
}