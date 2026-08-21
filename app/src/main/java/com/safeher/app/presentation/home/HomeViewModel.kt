package com.safeher.app.presentation.home

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.common.NetworkMonitor
import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.location.LocationState
import com.safeher.app.core.location.UserLocation
import com.safeher.app.core.permissions.PermissionManager
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.model.EmergencyStatus
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.model.UserProfile
import com.safeher.app.domain.repository.AuthRepository
import com.safeher.app.domain.repository.ContactRepository
import com.safeher.app.domain.repository.EmergencyRepository
import com.safeher.app.domain.repository.SettingsRepository
import com.safeher.app.domain.usecase.TriggerSosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val user: UserProfile? = null,
    val isOnline: Boolean = true,
    val locationState: LocationState = LocationState.Locating,
    val contacts: List<EmergencyContact> = emptyList(),
    val activeIncident: Incident? = null,
    val batteryLevel: Int = 100,
    val emergencyNumber: String = "112",
    val isSosTriggering: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val contactRepository: ContactRepository,
    private val emergencyRepository: EmergencyRepository,
    private val settingsRepository: SettingsRepository,
    private val locationClient: LocationClient,
    private val networkMonitor: NetworkMonitor,
    private val triggerSosUseCase: TriggerSosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var locationJob: Job? = null

    init {
        observeData()
        startLocationMonitoring()
        updateBatteryLevel()
    }

    private fun observeData() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(user = user) }
            }
        }

        viewModelScope.launch {
            contactRepository.getContacts().collect { contacts ->
                _uiState.update { it.copy(contacts = contacts) }
            }
        }

        viewModelScope.launch {
            emergencyRepository.getActiveEmergencyIncident().collect { incident ->
                _uiState.update { it.copy(activeIncident = incident) }
            }
        }

        viewModelScope.launch {
            settingsRepository.emergencyNumber.collect { num ->
                _uiState.update { it.copy(emergencyNumber = num) }
            }
        }

        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
    }

    fun startLocationMonitoring() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationClient.observeLocationState(5000L).collect { state ->
                if (state is LocationState.Available && state.location.address == null) {
                    val addr = locationClient.reverseGeocode(state.location.latitude, state.location.longitude)
                    _uiState.update {
                        it.copy(locationState = LocationState.Available(state.location.copy(address = addr)))
                    }
                } else {
                    _uiState.update { it.copy(locationState = state) }
                }
            }
        }
    }

    fun onResume() {
        updateBatteryLevel()
        startLocationMonitoring()
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            startLocationMonitoring()
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

    fun triggerSos(onSuccess: (incidentId: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSosTriggering = true, error = null) }
            val result = triggerSosUseCase()
            when (result) {
                is Resource.Success -> {
                    _uiState.update { it.copy(isSosTriggering = false, activeIncident = result.data) }
                    onSuccess(result.data.id)
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isSosTriggering = false, error = result.message) }
                }
                else -> {
                    _uiState.update { it.copy(isSosTriggering = false) }
                }
            }
        }
    }

    private fun updateBatteryLevel() {
        val battery = getBatteryPercentage()
        _uiState.update { it.copy(batteryLevel = battery) }
    }

    private fun getBatteryPercentage(): Int {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 100
    }
}