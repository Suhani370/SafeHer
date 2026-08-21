package com.safeher.app.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Looper
import android.provider.Settings
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.safeher.app.core.permissions.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale

interface LocationClient {
    fun hasLocationPermission(): Boolean
    fun isLocationEnabled(): Boolean
    fun getLocationUpdates(intervalMs: Long = 5000L): Flow<UserLocation>
    fun observeLocationState(intervalMs: Long = 5000L): Flow<LocationState>
    suspend fun getLastKnownLocation(): UserLocation?
    suspend fun getFreshLocation(): UserLocation?
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String?
    fun openLocationSettings()
    fun openAppSettings()
}

class DefaultLocationClient(
    private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationClient {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun hasLocationPermission(): Boolean {
        return PermissionManager.hasLocationPermission(context)
    }

    override fun isLocationEnabled(): Boolean {
        return try {
            LocationManagerCompat.isLocationEnabled(locationManager)
        } catch (e: Exception) {
            try {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (ex: Exception) {
                false
            }
        }
    }

    override fun openLocationSettings() {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            openAppSettings()
        }
    }

    override fun openAppSettings() {
        PermissionManager.openAppSettings(context)
    }

    @SuppressLint("MissingPermission")
    override fun observeLocationState(intervalMs: Long): Flow<LocationState> = callbackFlow {
        if (!hasLocationPermission()) {
            trySend(LocationState.PermissionRequired)
            awaitClose { }
            return@callbackFlow
        }

        if (!isLocationEnabled()) {
            trySend(LocationState.LocationDisabled)
            awaitClose { }
            return@callbackFlow
        }

        trySend(LocationState.Locating)

        // Try getting cached / quick fresh location first
        try {
            val lastLoc = getLastKnownLocation()
            if (lastLoc != null) {
                val addr = reverseGeocode(lastLoc.latitude, lastLoc.longitude)
                trySend(LocationState.Available(lastLoc.copy(address = addr)))
            }
        } catch (e: Exception) {
            // Non-fatal, continuous updates will follow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setMaxUpdateDelayMillis(intervalMs)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    val userLoc = location.toUserLocation()
                    // Reverse geocode asynchronously
                    trySend(LocationState.Available(userLoc))
                }
            }

            override fun onLocationAvailability(availability: com.google.android.gms.location.LocationAvailability) {
                if (!availability.isLocationAvailable && !isLocationEnabled()) {
                    trySend(LocationState.LocationDisabled)
                }
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            trySend(LocationState.PermissionRequired)
        } catch (e: Exception) {
            trySend(LocationState.Error(e.localizedMessage ?: "Failed to start location updates"))
        }

        awaitClose {
            try {
                client.removeLocationUpdates(callback)
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }.distinctUntilChanged()

    @SuppressLint("MissingPermission")
    override fun getLocationUpdates(intervalMs: Long): Flow<UserLocation> = callbackFlow {
        if (!hasLocationPermission()) {
            awaitClose { }
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
            .setMinUpdateIntervalMillis(intervalMs / 2)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(location.toUserLocation())
                }
            }
        }

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        } catch (e: Exception) {
            // Ignored
        }

        awaitClose {
            try {
                client.removeLocationUpdates(callback)
            } catch (e: Exception) {
                // Ignored
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getLastKnownLocation(): UserLocation? {
        if (!hasLocationPermission()) return null
        return try {
            val loc = client.lastLocation.await()
            loc?.toUserLocation()
        } catch (e: Exception) {
            null
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun getFreshLocation(): UserLocation? {
        if (!hasLocationPermission() || !isLocationEnabled()) {
            return getLastKnownLocation()
        }
        return try {
            val cts = CancellationTokenSource()
            val request = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMaxUpdateAgeMillis(10000)
                .build()
            val loc = client.getCurrentLocation(request, cts.token).await()
            loc?.toUserLocation() ?: getLastKnownLocation()
        } catch (e: Exception) {
            getLastKnownLocation()
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = addresses?.firstOrNull()
                    formatAddress(addr)
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val addr = addresses?.firstOrNull()
                    formatAddress(addr)
                }
            } catch (e: Exception) {
                null
            }
        }

    private fun formatAddress(address: android.location.Address?): String? {
        if (address == null) return null
        val thoroughfare = address.thoroughfare
        val subLocality = address.subLocality
        val locality = address.locality
        val adminArea = address.adminArea

        val parts = listOfNotNull(
            thoroughfare,
            subLocality,
            locality,
            adminArea
        ).filter { it.isNotBlank() }

        return if (parts.isNotEmpty()) {
            parts.joinToString(", ")
        } else {
            address.getAddressLine(0)
        }
    }

    private fun Location.toUserLocation(): UserLocation {
        return UserLocation(
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            speed = speed,
            bearing = bearing,
            altitude = altitude,
            timestamp = time
        )
    }
}