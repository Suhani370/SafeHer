package com.safeher.app.presentation.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safeher.app.core.common.Resource
import com.safeher.app.core.security.PhoneValidator
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.repository.AuthRepository
import com.safeher.app.domain.usecase.ManageContactsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<EmergencyContact> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val manageContactsUseCase: ManageContactsUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            manageContactsUseCase.getContacts().collect { list ->
                _uiState.update { it.copy(contacts = list) }
            }
        }
    }

    fun addContact(name: String, phone: String, relationship: String, isPrimary: Boolean) {
        val sanitizedPhone = PhoneValidator.sanitizePhoneNumber(phone)
        if (name.isBlank() || !PhoneValidator.isValidPhoneNumber(sanitizedPhone)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid name and phone number.") }
            return
        }

        viewModelScope.launch {
            val contact = EmergencyContact(
                name = name.trim(),
                phoneNumber = sanitizedPhone,
                relationship = relationship.trim().ifBlank { "Trusted Contact" },
                isPrimary = isPrimary,
                priorityOrder = if (isPrimary) 1 else _uiState.value.contacts.size + 1
            )
            val result = manageContactsUseCase.addContact(contact)
            if (result is Resource.Success) {
                _uiState.update { it.copy(successMessage = "Contact added successfully.") }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            manageContactsUseCase.updateContact(contact)
        }
    }

    fun deleteContact(id: Long) {
        viewModelScope.launch {
            manageContactsUseCase.deleteContact(id)
            _uiState.update { it.copy(successMessage = "Contact removed.") }
        }
    }

    fun testAlert(contact: EmergencyContact) {
        viewModelScope.launch {
            val user = authRepository.currentUser.firstOrNull()
            val userName = user?.fullName ?: "SafeHer User"
            val result = manageContactsUseCase.testAlert(contact, userName)
            if (result is Resource.Success) {
                _uiState.update { it.copy(successMessage = "Test alert sent to ${contact.name}.") }
            } else if (result is Resource.Error) {
                _uiState.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
