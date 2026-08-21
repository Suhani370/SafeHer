package com.safeher.app.domain.usecase

import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.security.SecurityUtils
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.model.UserProfile
import com.safeher.app.domain.repository.AuthRepository
import com.safeher.app.domain.repository.ContactRepository
import com.safeher.app.domain.repository.EmergencyRepository
import com.safeher.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class TriggerSosUseCase @Inject constructor(
    private val emergencyRepository: EmergencyRepository,
    private val contactRepository: ContactRepository,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val locationClient: LocationClient
) {
    suspend operator fun invoke(): Resource<Incident> {
        // 1. Acquire current location with high-accuracy, fallback to last known
        val location = locationClient.getFreshLocation()

        // 2. Fetch current user info for message metadata
        val user = authRepository.currentUser.firstOrNull()
        val userName = user?.fullName ?: "SafeHer User"

        // 3. Reverse geocode if coordinate is available
        val address = if (location != null) {
            locationClient.reverseGeocode(location.latitude, location.longitude)
        } else null

        val resolvedLocation = location?.copy(address = address)

        // 4. Trigger SOS in Emergency Repository (Local Room write + Firestore queue)
        val result = emergencyRepository.triggerSos(resolvedLocation, userName)

        if (result is Resource.Success) {
            // 5. Send SMS if auto-SMS enabled
            val autoSms = settingsRepository.isAutoSmsEnabled.firstOrNull() ?: true
            if (autoSms) {
                val contacts = contactRepository.getContactsSync()
                if (contacts.isNotEmpty()) {
                    val smsBody = SecurityUtils.generateSosSmsBody(
                        userName = userName,
                        latitude = resolvedLocation?.latitude,
                        longitude = resolvedLocation?.longitude,
                        address = address
                    )
                    emergencyRepository.broadcastEmergencySms(contacts, smsBody)
                }
            }
        }

        return result
    }
}

class CancelSosUseCase @Inject constructor(
    private val emergencyRepository: EmergencyRepository
) {
    suspend operator fun invoke(incidentId: String, reason: String = "User marked safe"): Resource<Unit> {
        return emergencyRepository.cancelSos(incidentId, reason)
    }
}

class ManageContactsUseCase @Inject constructor(
    private val contactRepository: ContactRepository
) {
    fun getContacts() = contactRepository.getContacts()
    suspend fun addContact(contact: com.safeher.app.domain.model.EmergencyContact) = contactRepository.addContact(contact)
    suspend fun updateContact(contact: com.safeher.app.domain.model.EmergencyContact) = contactRepository.updateContact(contact)
    suspend fun deleteContact(id: Long) = contactRepository.deleteContact(id)
    suspend fun testAlert(contact: com.safeher.app.domain.model.EmergencyContact, userName: String) =
        contactRepository.testAlertContact(contact, userName)
}

class GetNearbyHelpUseCase @Inject constructor(
    private val nearbyRepository: com.safeher.app.domain.repository.NearbyRepository,
    private val locationClient: LocationClient
) {
    suspend operator fun invoke(
        category: com.safeher.app.domain.model.PlaceCategory? = null,
        radiusMeters: Int = 5000
    ): Resource<List<com.safeher.app.domain.model.NearbyPlace>> {
        val location = locationClient.getFreshLocation()
            ?: return Resource.Error("Unable to acquire current location for nearby search")

        return nearbyRepository.getNearbyEmergencyPlaces(
            latitude = location.latitude,
            longitude = location.longitude,
            radiusMeters = radiusMeters,
            category = category
        )
    }
}
