package com.safeher.app.presentation.journey

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.location.UserLocation
import com.safeher.app.domain.model.Journey
import com.safeher.app.service.LocationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class JourneyUiState(
    val activeJourney: Journey? = null,
    val currentLocation: UserLocation? = null,
    val isTracking: Boolean = false,
    val distanceRemainingMeters: Float = 0f,
    val estimatedArrivalMinutes: Int = 0
)

@HiltViewModel
class JourneyViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationClient: LocationClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyUiState())
    val uiState: StateFlow<JourneyUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null

    fun startJourney(destinationName: String, expectedArrivalMinutes: Int) {
        val journeyId = UUID.randomUUID().toString()
        viewModelScope.launch {
            val freshLoc = locationClient.getFreshLocation()
            val journey = Journey(
                id = journeyId,
                destinationName = destinationName,
                destLatitude = (freshLoc?.latitude ?: 0.0) + 0.01,
                destLongitude = (freshLoc?.longitude ?: 0.0) + 0.01,
                estimatedArrivalMinutes = expectedArrivalMinutes,
                startTime = System.currentTimeMillis(),
                isActive = true
            )

            _uiState.update {
                it.copy(
                    activeJourney = journey,
                    isTracking = true,
                    currentLocation = freshLoc,
                    estimatedArrivalMinutes = expectedArrivalMinutes
                )
            }

            val intent = Intent(context, LocationForegroundService::class.java).apply {
                action = LocationForegroundService.ACTION_START_JOURNEY_TRACKING
                putExtra(LocationForegroundService.EXTRA_INCIDENT_ID, journeyId)
            }
            context.startForegroundService(intent)

            startTracking()
        }
    }

    private fun startTracking() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationClient.getLocationUpdates(5000L).collect { loc ->
                _uiState.update { it.copy(currentLocation = loc) }
            }
        }
    }

    fun endJourney() {
        val intent = Intent(context, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_STOP_TRACKING
        }
        context.startService(intent)

        locationJob?.cancel()
        _uiState.update {
            it.copy(activeJourney = null, isTracking = false)
        }
    }
}
