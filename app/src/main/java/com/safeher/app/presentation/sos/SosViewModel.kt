package com.safeher.app.presentation.sos

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.audio.AudioRecorderManager
import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.location.UserLocation
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.repository.ContactRepository
import com.safeher.app.domain.repository.EmergencyRepository
import com.safeher.app.domain.repository.IncidentRepository
import com.safeher.app.domain.repository.SettingsRepository
import com.safeher.app.domain.usecase.CancelSosUseCase
import com.safeher.app.service.AudioRecordingService
import com.safeher.app.service.LocationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SosActiveUiState(
    val incident: Incident? = null,
    val elapsedSeconds: Long = 0L,
    val liveLocation: UserLocation? = null,
    val contacts: List<EmergencyContact> = emptyList(),
    val emergencyNumber: String = "112",
    val isRecordingAudio: Boolean = false,
    val isCancelling: Boolean = false,
    val isCancelled: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SosViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val incidentRepository: IncidentRepository,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository,
    private val locationClient: LocationClient,
    private val audioRecorderManager: AudioRecorderManager,
    private val cancelSosUseCase: CancelSosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SosActiveUiState())
    val uiState: StateFlow<SosActiveUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var locationJob: Job? = null

    fun loadActiveSos(incidentId: String) {
        viewModelScope.launch {
            val incident = incidentRepository.getIncidentById(incidentId)
            val contacts = contactRepository.getContactsSync()

            _uiState.update {
                it.copy(
                    incident = incident,
                    contacts = contacts,
                    isRecordingAudio = audioRecorderManager.isRecording()
                )
            }

            // Start Foreground Location Service
            val serviceIntent = Intent(context, LocationForegroundService::class.java).apply {
                action = LocationForegroundService.ACTION_START_SOS_TRACKING
                putExtra(LocationForegroundService.EXTRA_INCIDENT_ID, incidentId)
            }
            try {
                context.startForegroundService(serviceIntent)
            } catch (e: Exception) {
                // Fallback for Android 14 foreground restrictions if app was backgrounded
            }
        }

        viewModelScope.launch {
            settingsRepository.isAutoAudioRecordingEnabled.collect { autoRecord ->
                if (autoRecord && !audioRecorderManager.isRecording()) {
                    startAudioRecording()
                }
            }
        }

        startTimer()
        startLiveLocationStream()

        viewModelScope.launch {
            settingsRepository.emergencyNumber.collect { num ->
                _uiState.update { it.copy(emergencyNumber = num) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
            }
        }
    }

    private fun startLiveLocationStream() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            locationClient.getLocationUpdates(5000L).collect { loc ->
                val addr = locationClient.reverseGeocode(loc.latitude, loc.longitude)
                _uiState.update { it.copy(liveLocation = loc.copy(address = addr)) }
            }
        }
    }

    fun startAudioRecording() {
        val recordIntent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_START_RECORDING
        }
        context.startForegroundService(recordIntent)
        _uiState.update { it.copy(isRecordingAudio = true) }
    }

    fun stopAudioRecording() {
        val recordIntent = Intent(context, AudioRecordingService::class.java).apply {
            action = AudioRecordingService.ACTION_STOP_RECORDING
        }
        context.startService(recordIntent)
        _uiState.update { it.copy(isRecordingAudio = false) }
    }

    fun stopSos(notes: String = "User marked safe") {
        val incId = _uiState.value.incident?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }

            // Stop Foreground Services
            val locationIntent = Intent(context, LocationForegroundService::class.java).apply {
                action = LocationForegroundService.ACTION_STOP_TRACKING
            }
            context.startService(locationIntent)

            stopAudioRecording()

            val result = cancelSosUseCase(incId, notes)
            when (result) {
                is Resource.Success -> {
                    timerJob?.cancel()
                    locationJob?.cancel()
                    _uiState.update { it.copy(isCancelling = false, isCancelled = true) }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isCancelling = false, error = result.message) }
                }
                else -> {}
            }
        }
    }
}
