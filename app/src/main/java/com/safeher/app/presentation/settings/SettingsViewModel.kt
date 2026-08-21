package com.safeher.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val emergencyNumber: String = "112",
    val isAutoSmsEnabled: Boolean = true,
    val isAutoAudioRecordingEnabled: Boolean = false,
    val isDiscreetModeEnabled: Boolean = false,
    val discreetPin: String = "9999"
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.emergencyNumber.collect { num ->
                _uiState.update { it.copy(emergencyNumber = num) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isAutoSmsEnabled.collect { sms ->
                _uiState.update { it.copy(isAutoSmsEnabled = sms) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isAutoAudioRecordingEnabled.collect { rec ->
                _uiState.update { it.copy(isAutoAudioRecordingEnabled = rec) }
            }
        }
        viewModelScope.launch {
            settingsRepository.isDiscreetModeEnabled.collect { dis ->
                _uiState.update { it.copy(isDiscreetModeEnabled = dis) }
            }
        }
        viewModelScope.launch {
            settingsRepository.discreetPin.collect { pin ->
                _uiState.update { it.copy(discreetPin = pin) }
            }
        }
    }

    fun setEmergencyNumber(number: String) {
        viewModelScope.launch {
            settingsRepository.setEmergencyNumber(number.trim())
        }
    }

    fun setAutoSmsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSmsEnabled(enabled)
        }
    }

    fun setAutoAudioRecordingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoAudioRecordingEnabled(enabled)
        }
    }

    fun setDiscreetModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDiscreetModeEnabled(enabled)
        }
    }

    fun setDiscreetPin(pin: String) {
        viewModelScope.launch {
            settingsRepository.setDiscreetPin(pin.trim())
        }
    }
}
