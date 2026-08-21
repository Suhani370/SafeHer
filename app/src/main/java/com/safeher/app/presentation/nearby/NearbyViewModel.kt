package com.safeher.app.presentation.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.location.LocationState
import com.safeher.app.domain.model.NearbyPlace
import com.safeher.app.domain.model.PlaceCategory
import com.safeher.app.domain.repository.NearbyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NearbyUiState(
    val locationState: LocationState = LocationState.Locating,
    val places: List<NearbyPlace> = emptyList(),
    val selectedCategory: PlaceCategory? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class NearbyViewModel @Inject constructor(
    private val locationClient: LocationClient,
    private val nearbyRepository: NearbyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NearbyUiState())
    val uiState: StateFlow<NearbyUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null
    private var lastKnownLat: Double? = null
    private var lastKnownLng: Double? = null

    init {
        startLocationObservation()
    }

    fun startLocationObservation() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationClient.observeLocationState(10000L).collect { state ->
                _uiState.update { it.copy(locationState = state) }
                if (state is LocationState.Available) {
                    val lat = state.location.latitude
                    val lng = state.location.longitude

                    // Only fetch if coordinates have changed significantly or first load
                    if (lastKnownLat == null || _uiState.value.places.isEmpty()) {
                        lastKnownLat = lat
                        lastKnownLng = lng
                        fetchPlaces(lat, lng, _uiState.value.selectedCategory)
                    }
                }
            }
        }
    }

    fun selectCategory(category: PlaceCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        val lat = lastKnownLat
        val lng = lastKnownLng
        if (lat != null && lng != null) {
            fetchPlaces(lat, lng, category)
        } else if (_uiState.value.locationState is LocationState.Available) {
            val loc = (_uiState.value.locationState as LocationState.Available).location
            fetchPlaces(loc.latitude, loc.longitude, category)
        }
    }

    fun refresh() {
        val lat = lastKnownLat
        val lng = lastKnownLng
        if (lat != null && lng != null) {
            fetchPlaces(lat, lng, _uiState.value.selectedCategory)
        } else {
            startLocationObservation()
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            startLocationObservation()
        } else {
            _uiState.update { it.copy(locationState = LocationState.PermissionRequired) }
        }
    }

    fun openLocationSettings() {
        locationClient.openLocationSettings()
    }

    fun openAppSettings() {
        locationClient.openAppSettings()
    }

    private fun fetchPlaces(latitude: Double, longitude: Double, category: PlaceCategory?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = nearbyRepository.getNearbyEmergencyPlaces(
                latitude = latitude,
                longitude = longitude,
                radiusMeters = 5000,
                category = category
            )
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isLoading = false, places = result.data) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}