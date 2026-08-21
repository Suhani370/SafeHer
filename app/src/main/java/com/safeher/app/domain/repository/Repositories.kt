package com.safeher.app.domain.repository

import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.UserLocation
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.model.NearbyPlace
import com.safeher.app.domain.model.PlaceCategory
import com.safeher.app.domain.model.SafetyTimer
import com.safeher.app.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    fun isUserLoggedIn(): Boolean
    suspend fun login(email: String, password: String): Resource<UserProfile>
    suspend fun register(name: String, email: String, password: String, phone: String): Resource<UserProfile>
    suspend fun sendPasswordReset(email: String): Resource<Unit>
    suspend fun logout(): Resource<Unit>
    suspend fun updateUserProfile(profile: UserProfile): Resource<Unit>
    suspend fun deleteAccount(): Resource<Unit>
}

interface ContactRepository {
    fun getContacts(): Flow<List<EmergencyContact>>
    suspend fun getContactsSync(): List<EmergencyContact>
    suspend fun addContact(contact: EmergencyContact): Resource<Long>
    suspend fun updateContact(contact: EmergencyContact): Resource<Unit>
    suspend fun deleteContact(id: Long): Resource<Unit>
    suspend fun testAlertContact(contact: EmergencyContact, userName: String): Resource<Unit>
}

interface EmergencyRepository {
    fun getActiveEmergencyIncident(): Flow<Incident?>
    suspend fun triggerSos(location: UserLocation?, userName: String): Resource<Incident>
    suspend fun cancelSos(incidentId: String, notes: String): Resource<Unit>
    suspend fun broadcastEmergencySms(contacts: List<EmergencyContact>, message: String): Int
    suspend fun syncPendingIncidents(): Resource<Int>
}

interface IncidentRepository {
    fun getIncidents(): Flow<List<Incident>>
    suspend fun getIncidentById(id: String): Incident?
    suspend fun saveIncident(incident: Incident): Resource<Unit>
    suspend fun deleteIncident(id: String): Resource<Unit>
}

interface SettingsRepository {
    val emergencyNumber: Flow<String>
    val isAutoSmsEnabled: Flow<Boolean>
    val isAutoAudioRecordingEnabled: Flow<Boolean>
    val isDiscreetModeEnabled: Flow<Boolean>
    val discreetPin: Flow<String>
    suspend fun setEmergencyNumber(number: String)
    suspend fun setAutoSmsEnabled(enabled: Boolean)
    suspend fun setAutoAudioRecordingEnabled(enabled: Boolean)
    suspend fun setDiscreetModeEnabled(enabled: Boolean)
    suspend fun setDiscreetPin(pin: String)
}

interface NearbyRepository {
    suspend fun getNearbyEmergencyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int = 5000,
        category: PlaceCategory? = null
    ): Resource<List<NearbyPlace>>
}
