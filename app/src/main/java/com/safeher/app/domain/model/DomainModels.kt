package com.safeher.app.domain.model

data class UserProfile(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val bloodGroup: String = "",
    val emergencyNote: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class EmergencyContact(
    val id: Long = 0,
    val name: String,
    val phoneNumber: String,
    val relationship: String,
    val isPrimary: Boolean = false,
    val priorityOrder: Int = 1,
    val isSynced: Boolean = false
)

enum class IncidentType {
    SOS,
    SAFETY_TIMER_EXPIRED,
    JOURNEY_DEVIATION,
    MANUAL_EVIDENCE,
    TEST_ALERT
}

enum class IncidentStatus {
    ACTIVE,
    RESOLVED,
    CANCELLED
}

data class Incident(
    val id: String,
    val type: IncidentType,
    val status: IncidentStatus,
    val timestamp: Long,
    val latitude: Double?,
    val longitude: Double?,
    val address: String?,
    val audioEvidencePath: String? = null,
    val notes: String = "",
    val durationSeconds: Long = 0L,
    val contactsNotifiedCount: Int = 0,
    val isSynced: Boolean = false
)

enum class TimerState {
    IDLE,
    RUNNING,
    EXPIRED_COUNTDOWN,
    RESOLVED_SAFE,
    ESCALATED_SOS
}

data class SafetyTimer(
    val id: Long = 1L,
    val durationMinutes: Int,
    val remainingSeconds: Int,
    val destinationName: String = "",
    val state: TimerState = TimerState.IDLE,
    val startedAt: Long = 0L,
    val escalationGraceSeconds: Int = 30
)

data class Journey(
    val id: String,
    val destinationName: String,
    val destLatitude: Double,
    val destLongitude: Double,
    val estimatedArrivalMinutes: Int,
    val startTime: Long,
    val isActive: Boolean = true
)

enum class PlaceCategory {
    POLICE,
    HOSPITAL,
    PHARMACY,
    WOMEN_SHELTER,
    FIRE_STATION
}

data class NearbyPlace(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val phoneNumber: String? = null,
    val distanceMeters: Float = 0f,
    val isOpenNow: Boolean? = null
)

sealed class EmergencyStatus {
    data object Safe : EmergencyStatus()
    data class ActiveSos(val incidentId: String, val startedAt: Long) : EmergencyStatus()
    data class TimerWarning(val remainingSeconds: Int) : EmergencyStatus()
}
