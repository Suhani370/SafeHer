package com.safeher.app.data.repository

import android.content.Context
import android.telephony.SmsManager
import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.UserLocation
import com.safeher.app.core.security.SecurityUtils
import com.safeher.app.data.local.dao.ContactDao
import com.safeher.app.data.local.dao.IncidentDao
import com.safeher.app.data.local.dao.LocationWaypointDao
import com.safeher.app.data.local.dao.PendingSyncDao
import com.safeher.app.data.local.dao.UserDao
import com.safeher.app.data.local.entity.EmergencyContactEntity
import com.safeher.app.data.local.entity.IncidentEntity
import com.safeher.app.data.local.entity.LocationWaypointEntity
import com.safeher.app.data.local.entity.PendingSyncEntity
import com.safeher.app.data.local.entity.UserEntity
import com.safeher.app.data.local.preferences.UserPreferencesDataStore
import com.safeher.app.data.remote.firebase.FirebaseAuthSource
import com.safeher.app.data.remote.firebase.FirestoreEmergencySource
import com.safeher.app.data.remote.places.NearbyHelpDataSource
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.model.IncidentStatus
import com.safeher.app.domain.model.IncidentType
import com.safeher.app.domain.model.NearbyPlace
import com.safeher.app.domain.model.PlaceCategory
import com.safeher.app.domain.model.UserProfile
import com.safeher.app.domain.repository.AuthRepository
import com.safeher.app.domain.repository.ContactRepository
import com.safeher.app.domain.repository.EmergencyRepository
import com.safeher.app.domain.repository.IncidentRepository
import com.safeher.app.domain.repository.NearbyRepository
import com.safeher.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

class AuthRepositoryImpl(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuthSource,
    private val firestoreSource: FirestoreEmergencySource,
    private val preferences: UserPreferencesDataStore
) : AuthRepository {

    override val currentUser: Flow<UserProfile?> = userDao.getUserProfile().map { it?.toDomain() }

    override fun isUserLoggedIn(): Boolean {
        return firebaseAuth.currentUserId != null
    }

    override suspend fun login(email: String, password: String): Resource<UserProfile> {
        val result = firebaseAuth.signIn(email, password)
        return when (result) {
            is Resource.Success -> {
                val uid = result.data
                val profile = UserProfile(id = uid, email = email, fullName = email.substringBefore("@"))
                userDao.insertOrUpdateUser(UserEntity.fromDomain(profile))
                preferences.setUserLoggedIn(true)
                Resource.Success(profile)
            }
            is Resource.Error -> {
                // If offline or Firebase disabled, check if local user exists
                val localUser = userDao.getUserProfileSync()
                if (localUser != null && localUser.email.equals(email, ignoreCase = true)) {
                    preferences.setUserLoggedIn(true)
                    Resource.Success(localUser.toDomain())
                } else {
                    Resource.Error(result.message, result.cause)
                }
            }
            else -> Resource.Error("Unknown login response")
        }
    }

    override suspend fun register(name: String, email: String, password: String, phone: String): Resource<UserProfile> {
        val result = firebaseAuth.signUp(email, password)
        val uid = if (result is Resource.Success) result.data else UUID.randomUUID().toString()
        val profile = UserProfile(id = uid, fullName = name, email = email, phoneNumber = phone)

        userDao.insertOrUpdateUser(UserEntity.fromDomain(profile))
        firestoreSource.saveUserProfile(uid, profile)
        preferences.setUserLoggedIn(true)
        return Resource.Success(profile)
    }

    override suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return firebaseAuth.sendPasswordReset(email)
    }

    override suspend fun logout(): Resource<Unit> {
        firebaseAuth.signOut()
        userDao.clearUser()
        preferences.setUserLoggedIn(false)
        return Resource.Success(Unit)
    }

    override suspend fun updateUserProfile(profile: UserProfile): Resource<Unit> {
        userDao.insertOrUpdateUser(UserEntity.fromDomain(profile))
        firestoreSource.saveUserProfile(profile.id, profile)
        return Resource.Success(Unit)
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        userDao.clearUser()
        preferences.clearAll()
        firebaseAuth.signOut()
        return Resource.Success(Unit)
    }
}

class ContactRepositoryImpl(
    private val context: Context,
    private val contactDao: ContactDao
) : ContactRepository {

    override fun getContacts(): Flow<List<EmergencyContact>> {
        return contactDao.getAllContacts().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getContactsSync(): List<EmergencyContact> {
        return contactDao.getAllContactsSync().map { it.toDomain() }
    }

    override suspend fun addContact(contact: EmergencyContact): Resource<Long> {
        return try {
            val id = contactDao.insertContact(EmergencyContactEntity.fromDomain(contact))
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error("Failed to add contact", e)
        }
    }

    override suspend fun updateContact(contact: EmergencyContact): Resource<Unit> {
        return try {
            contactDao.updateContact(EmergencyContactEntity.fromDomain(contact))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to update contact", e)
        }
    }

    override suspend fun deleteContact(id: Long): Resource<Unit> {
        return try {
            contactDao.deleteContactById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete contact", e)
        }
    }

    override suspend fun testAlertContact(contact: EmergencyContact, userName: String): Resource<Unit> {
        return try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: @Suppress("DEPRECATION") SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            val text = SecurityUtils.generateTestSmsBody(userName)
            val parts = smsManager.divideMessage(text)
            smsManager.sendMultipartTextMessage(contact.phoneNumber, null, parts, null, null)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Could not send SMS test: ${e.message}", e)
        }
    }
}

class EmergencyRepositoryImpl(
    private val context: Context,
    private val incidentDao: IncidentDao,
    private val contactDao: ContactDao,
    private val pendingSyncDao: PendingSyncDao,
    private val firestoreSource: FirestoreEmergencySource,
    private val firebaseAuth: FirebaseAuthSource
) : EmergencyRepository {

    override fun getActiveEmergencyIncident(): Flow<Incident?> {
        return incidentDao.getActiveEmergency().map { it?.toDomain() }
    }

    override suspend fun triggerSos(location: UserLocation?, userName: String): Resource<Incident> {
        val incidentId = UUID.randomUUID().toString()
        val contacts = contactDao.getAllContactsSync()

        val incidentEntity = IncidentEntity(
            id = incidentId,
            type = IncidentType.SOS.name,
            status = IncidentStatus.ACTIVE.name,
            timestamp = System.currentTimeMillis(),
            latitude = location?.latitude,
            longitude = location?.longitude,
            address = location?.address,
            audioEvidencePath = null,
            notes = "SOS Triggered by $userName",
            durationSeconds = 0L,
            contactsNotifiedCount = contacts.size,
            isSynced = false
        )

        // 1. Immediately persist to Room local database
        incidentDao.insertIncident(incidentEntity)

        // 2. Queue for background sync
        pendingSyncDao.enqueue(
            PendingSyncEntity(
                eventType = "INCIDENT",
                payloadJson = incidentId
            )
        )

        // 3. Attempt direct cloud upload if user has ID
        val uid = firebaseAuth.currentUserId
        if (uid != null) {
            try {
                firestoreSource.uploadIncident(uid, incidentEntity)
                incidentDao.updateIncident(incidentEntity.copy(isSynced = true))
            } catch (e: Exception) {
                // Keep unsynced; Worker will retry
            }
        }

        return Resource.Success(incidentEntity.toDomain())
    }

    override suspend fun cancelSos(incidentId: String, notes: String): Resource<Unit> {
        val incident = incidentDao.getIncidentById(incidentId) ?: return Resource.Error("Incident not found")
        val duration = (System.currentTimeMillis() - incident.timestamp) / 1000
        val updated = incident.copy(
            status = IncidentStatus.CANCELLED.name,
            notes = notes,
            durationSeconds = duration,
            isSynced = false
        )
        incidentDao.updateIncident(updated)

        val uid = firebaseAuth.currentUserId
        if (uid != null) {
            try {
                firestoreSource.uploadIncident(uid, updated)
                incidentDao.updateIncident(updated.copy(isSynced = true))
            } catch (e: Exception) {
                // Handled via sync queue
            }
        }

        return Resource.Success(Unit)
    }

    override suspend fun broadcastEmergencySms(contacts: List<EmergencyContact>, message: String): Int {
        var sentCount = 0
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java) ?: @Suppress("DEPRECATION") SmsManager.getDefault()
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            for (c in contacts) {
                try {
                    val parts = smsManager.divideMessage(message)
                    smsManager.sendMultipartTextMessage(c.phoneNumber, null, parts, null, null)
                    sentCount++
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return sentCount
    }

    override suspend fun syncPendingIncidents(): Resource<Int> {
        val unsynced = incidentDao.getUnsyncedIncidents()
        val uid = firebaseAuth.currentUserId ?: return Resource.Error("User not logged in for cloud sync")
        var count = 0
        for (inc in unsynced) {
            val res = firestoreSource.uploadIncident(uid, inc)
            if (res is Resource.Success) {
                incidentDao.updateIncident(inc.copy(isSynced = true))
                count++
            }
        }
        return Resource.Success(count)
    }
}

class IncidentRepositoryImpl(
    private val incidentDao: IncidentDao
) : IncidentRepository {
    override fun getIncidents(): Flow<List<Incident>> {
        return incidentDao.getAllIncidents().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getIncidentById(id: String): Incident? {
        return incidentDao.getIncidentById(id)?.toDomain()
    }

    override suspend fun saveIncident(incident: Incident): Resource<Unit> {
        return try {
            incidentDao.insertIncident(IncidentEntity.fromDomain(incident))
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to save incident", e)
        }
    }

    override suspend fun deleteIncident(id: String): Resource<Unit> {
        return try {
            incidentDao.deleteIncidentById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to delete incident", e)
        }
    }
}

class SettingsRepositoryImpl(
    private val preferences: UserPreferencesDataStore
) : SettingsRepository {
    override val emergencyNumber: Flow<String> = preferences.emergencyNumber
    override val isAutoSmsEnabled: Flow<Boolean> = preferences.isAutoSmsEnabled
    override val isAutoAudioRecordingEnabled: Flow<Boolean> = preferences.isAutoAudioRecordingEnabled
    override val isDiscreetModeEnabled: Flow<Boolean> = preferences.isDiscreetModeEnabled
    override val discreetPin: Flow<String> = preferences.discreetPin

    override suspend fun setEmergencyNumber(number: String) = preferences.setEmergencyNumber(number)
    override suspend fun setAutoSmsEnabled(enabled: Boolean) = preferences.setAutoSmsEnabled(enabled)
    override suspend fun setAutoAudioRecordingEnabled(enabled: Boolean) = preferences.setAutoAudioRecordingEnabled(enabled)
    override suspend fun setDiscreetModeEnabled(enabled: Boolean) = preferences.setDiscreetModeEnabled(enabled)
    override suspend fun setDiscreetPin(pin: String) = preferences.setDiscreetPin(pin)
}

class NearbyRepositoryImpl(
    private val dataSource: NearbyHelpDataSource
) : NearbyRepository {
    override suspend fun getNearbyEmergencyPlaces(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int,
        category: PlaceCategory?
    ): Resource<List<NearbyPlace>> {
        return dataSource.fetchNearbyEmergencyPlaces(latitude, longitude, radiusMeters, category)
    }
}
