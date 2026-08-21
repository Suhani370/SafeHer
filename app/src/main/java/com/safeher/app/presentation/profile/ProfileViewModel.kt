package com.safeher.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.common.Resource
import com.safeher.app.domain.model.UserProfile
import com.safeher.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfile()
    }

    private fun observeProfile() {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.update { it.copy(profile = user) }
            }
        }
    }

    fun updateProfile(fullName: String, phone: String, bloodGroup: String, emergencyNote: String) {
        val current = _uiState.value.profile ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val updated = current.copy(
                fullName = fullName.trim(),
                phoneNumber = phone.trim(),
                bloodGroup = bloodGroup.trim(),
                emergencyNote = emergencyNote.trim()
            )
            authRepository.updateUserProfile(updated)
            _uiState.update { it.copy(isLoading = false, message = "Profile updated successfully.") }
        }
    }

    fun logout(onLogoutComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onLogoutComplete()
        }
    }
}
