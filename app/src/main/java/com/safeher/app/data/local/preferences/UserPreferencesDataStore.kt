package com.safeher.app.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.safeher.app.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "safeher_preferences")

class UserPreferencesDataStore(private val context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val KEY_EMERGENCY_NUMBER = stringPreferencesKey("emergency_number")
        val KEY_AUTO_SMS = booleanPreferencesKey("auto_sms_enabled")
        val KEY_AUTO_AUDIO_RECORD = booleanPreferencesKey("auto_audio_record_enabled")
        val KEY_DISCREET_MODE = booleanPreferencesKey("discreet_mode_enabled")
        val KEY_DISCREET_PIN = stringPreferencesKey("discreet_pin")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_LOGGED_IN = booleanPreferencesKey("user_logged_in")
    }

    val emergencyNumber: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_EMERGENCY_NUMBER] ?: BuildConfig.DEFAULT_EMERGENCY_NUMBER
    }

    val isAutoSmsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTO_SMS] ?: true
    }

    val isAutoAudioRecordingEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_AUTO_AUDIO_RECORD] ?: false
    }

    val isDiscreetModeEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DISCREET_MODE] ?: false
    }

    val discreetPin: Flow<String> = dataStore.data.map { prefs ->
        prefs[KEY_DISCREET_PIN] ?: "9999"
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val isUserLoggedIn: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_USER_LOGGED_IN] ?: false
    }

    suspend fun setEmergencyNumber(number: String) {
        dataStore.edit { it[KEY_EMERGENCY_NUMBER] = number }
    }

    suspend fun setAutoSmsEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_SMS] = enabled }
    }

    suspend fun setAutoAudioRecordingEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_AUTO_AUDIO_RECORD] = enabled }
    }

    suspend fun setDiscreetModeEnabled(enabled: Boolean) {
        dataStore.edit { it[KEY_DISCREET_MODE] = enabled }
    }

    suspend fun setDiscreetPin(pin: String) {
        dataStore.edit { it[KEY_DISCREET_PIN] = pin }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUserLoggedIn(loggedIn: Boolean) {
        dataStore.edit { it[KEY_USER_LOGGED_IN] = loggedIn }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
