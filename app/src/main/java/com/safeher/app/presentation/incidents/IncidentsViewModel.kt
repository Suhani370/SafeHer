package com.safeher.app.presentation.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class IncidentsUiState(
    val incidents: List<Incident> = emptyList(),
    val selectedIncident: Incident? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class IncidentsViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IncidentsUiState())
    val uiState: StateFlow<IncidentsUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
    }

    private fun loadIncidents() {
        viewModelScope.launch {
            incidentRepository.getIncidents().collect { list ->
                _uiState.update { it.copy(incidents = list) }
            }
        }
    }

    fun loadIncidentDetail(id: String) {
        viewModelScope.launch {
            val inc = incidentRepository.getIncidentById(id)
            _uiState.update { it.copy(selectedIncident = inc) }
        }
    }

    fun deleteIncident(id: String) {
        viewModelScope.launch {
            incidentRepository.deleteIncident(id)
        }
    }
}
