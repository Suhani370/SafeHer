package com.safeher.app.presentation.safetytimer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.common.Resource
import com.safeher.app.domain.model.SafetyTimer
import com.safeher.app.domain.model.TimerState
import com.safeher.app.domain.usecase.TriggerSosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SafetyTimerUiState(
    val timer: SafetyTimer = SafetyTimer(durationMinutes = 30, remainingSeconds = 1800),
    val isEscalating: Boolean = false,
    val escalatedIncidentId: String? = null,
    val error: String? = null
)

@HiltViewModel
class SafetyTimerViewModel @Inject constructor(
    private val triggerSosUseCase: TriggerSosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SafetyTimerUiState())
    val uiState: StateFlow<SafetyTimerUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var graceJob: Job? = null

    fun startTimer(durationMinutes: Int, destination: String) {
        val totalSeconds = durationMinutes * 60
        _uiState.update {
            it.copy(
                timer = SafetyTimer(
                    durationMinutes = durationMinutes,
                    remainingSeconds = totalSeconds,
                    destinationName = destination,
                    state = TimerState.RUNNING,
                    startedAt = System.currentTimeMillis()
                )
            )
        }

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (_uiState.value.timer.remainingSeconds > 0) {
                delay(1000)
                _uiState.update {
                    it.copy(timer = it.timer.copy(remainingSeconds = it.timer.remainingSeconds - 1))
                }
            }

            // Expiration reached -> Enter Grace Countdown
            startGraceCountdown()
        }
    }

    private fun startGraceCountdown() {
        _uiState.update {
            it.copy(timer = it.timer.copy(state = TimerState.EXPIRED_COUNTDOWN, remainingSeconds = 30))
        }

        graceJob?.cancel()
        graceJob = viewModelScope.launch {
            while (_uiState.value.timer.remainingSeconds > 0 && _uiState.value.timer.state == TimerState.EXPIRED_COUNTDOWN) {
                delay(1000)
                _uiState.update {
                    it.copy(timer = it.timer.copy(remainingSeconds = it.timer.remainingSeconds - 1))
                }
            }

            // If still unconfirmed after grace period, escalate to SOS
            if (_uiState.value.timer.state == TimerState.EXPIRED_COUNTDOWN) {
                escalateToSos()
            }
        }
    }

    private fun escalateToSos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isEscalating = true) }
            val result = triggerSosUseCase()
            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isEscalating = false,
                            escalatedIncidentId = result.data.id,
                            timer = it.timer.copy(state = TimerState.ESCALATED_SOS)
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isEscalating = false, error = result.message) }
                }
                else -> {}
            }
        }
    }

    fun confirmSafe() {
        countdownJob?.cancel()
        graceJob?.cancel()
        _uiState.update {
            it.copy(timer = it.timer.copy(state = TimerState.RESOLVED_SAFE, remainingSeconds = 0))
        }
    }

    fun resetTimer() {
        countdownJob?.cancel()
        graceJob?.cancel()
        _uiState.update {
            it.copy(
                timer = SafetyTimer(durationMinutes = 30, remainingSeconds = 1800, state = TimerState.IDLE),
                escalatedIncidentId = null
            )
        }
    }
}
