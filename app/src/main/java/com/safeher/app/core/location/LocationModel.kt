package com.safeher.app.core.location

data class UserLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val bearing: Float = 0f,
    val altitude: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String? = null
) {
    fun toMapsUrl(): String = "https://maps.google.com/?q=$latitude,$longitude"
    fun getFormattedCoordinates(): String = String.format(java.util.Locale.US, "%.5f, %.5f (±%dm)", latitude, longitude, accuracy.toInt())
}

sealed class LocationState {
    data object PermissionRequired : LocationState()
    data object LocationDisabled : LocationState()
    data object Locating : LocationState()
    data class Available(val location: UserLocation) : LocationState()
    data class Error(val message: String) : LocationState()
}