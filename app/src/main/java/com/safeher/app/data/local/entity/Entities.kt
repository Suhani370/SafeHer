package com.safeher.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.model.IncidentStatus
import com.safeher.app.domain.model.IncidentType
import com.safeher.app.domain.model.UserProfile

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val bloodGroup: String,
    val emergencyNote: String,
    val createdAt: Long
) {
    fun toDomain(): UserProfile = UserProfile(
        id = id,
        fullName = fullName,
        email = email,
        phoneNumber = phoneNumber,
        bloodGroup = bloodGroup,
        emergencyNote = emergencyNote,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(domain: UserProfile): UserEntity = UserEntity(
            id = domain.id,
            fullName = domain.fullName,
            email = domain.email,
            phoneNumber = domain.phoneNumber,
            bloodGroup = domain.bloodGroup,
            emergencyNote = domain.emergencyNote,
            createdAt = domain.createdAt
        )
    }
}

@Entity(tableName = "emergency_contacts")
data class EmergencyContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean,
    val priorityOrder: Int,
    val isSynced: Boolean = false
) {
    fun toDomain(): EmergencyContact = EmergencyContact(
        id = id,
        name = name,
        phoneNumber = phoneNumber,
        relationship = relationship,
        isPrimary = isPrimary,
        priorityOrder = priorityOrder,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(domain: EmergencyContact): EmergencyContactEntity = EmergencyContactEntity(
            id = domain.id,
            name = domain.name,
            phoneNumber = domain.phoneNumber,
            relationship = domain.relationship,
            isPrimary = domain.isPrimary,
            priorityOrder = domain.priorityOrder,
            isSynced = domain.isSynced
        )
    }
}

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val status: String,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val audioEvidencePath: String?,
    val notes: String,
    val durationSeconds: Long,
    val contactsNotifiedCount: Int,
    val isSynced: Boolean = false
) {
    fun toDomain(): Incident = Incident(
        id = id,
        type = try { IncidentType.valueOf(type) } catch (e: Exception) { IncidentType.SOS },
        status = try { IncidentStatus.valueOf(status) } catch (e: Exception) { IncidentStatus.ACTIVE },
        timestamp = timestamp,
        latitude = latitude,
        longitude = longitude,
        address = address,
        audioEvidencePath = audioEvidencePath,
        notes = notes,
        durationSeconds = durationSeconds,
        contactsNotifiedCount = contactsNotifiedCount,
        isSynced = isSynced
    )

    companion object {
        fun fromDomain(domain: Incident): IncidentEntity = IncidentEntity(
            id = domain.id,
            type = domain.type.name,
            status = domain.status.name,
            timestamp = domain.timestamp,
            latitude = domain.latitude,
            longitude = domain.longitude,
            address = domain.address,
            audioEvidencePath = domain.audioEvidencePath,
            notes = domain.notes,
            durationSeconds = domain.durationSeconds,
            contactsNotifiedCount = domain.contactsNotifiedCount,
            isSynced = domain.isSynced
        )
    }
}

@Entity(tableName = "pending_sync_queue")
data class PendingSyncEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventType: String, // "INCIDENT", "CONTACT", "LOCATION_POINT"
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0
)

@Entity(tableName = "location_waypoints")
data class LocationWaypointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val incidentId: String,
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long,
    val isSynced: Boolean = false
)
