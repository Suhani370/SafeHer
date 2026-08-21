package com.safeher.app

import com.safeher.app.core.common.Resource
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.location.UserLocation
import com.safeher.app.domain.model.EmergencyContact
import com.safeher.app.domain.model.Incident
import com.safeher.app.domain.model.IncidentStatus
import com.safeher.app.domain.model.IncidentType
import com.safeher.app.domain.model.UserProfile
import com.safeher.app.domain.repository.AuthRepository
import com.safeher.app.domain.repository.ContactRepository
import com.safeher.app.domain.repository.EmergencyRepository
import com.safeher.app.domain.repository.SettingsRepository
import com.safeher.app.domain.usecase.TriggerSosUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TriggerSosUseCaseTest {

    private lateinit var emergencyRepository: EmergencyRepository
    private lateinit var contactRepository: ContactRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var locationClient: LocationClient
    private lateinit var triggerSosUseCase: TriggerSosUseCase

    @Before
    fun setup() {
        emergencyRepository = mockk(relaxed = true)
        contactRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        locationClient = mockk(relaxed = true)

        triggerSosUseCase = TriggerSosUseCase(
            emergencyRepository = emergencyRepository,
            contactRepository = contactRepository,
            authRepository = authRepository,
            settingsRepository = settingsRepository,
            locationClient = locationClient
        )
    }

    @Test
    fun triggerSos_whenCalled_persistsIncidentAndBroadcastsSms() = runTest {
        val testLocation = UserLocation(latitude = 28.7041, longitude = 77.1025, accuracy = 10f)
        val testUser = UserProfile(id = "user1", fullName = "Ananya Singh", email = "ananya@test.com")
        val testContacts = listOf(
            EmergencyContact(id = 1, name = "Mother", phoneNumber = "+919876543210", relationship = "Mother", isPrimary = true)
        )
        val testIncident = Incident(
            id = "inc-101",
            type = IncidentType.SOS,
            status = IncidentStatus.ACTIVE,
            timestamp = System.currentTimeMillis(),
            latitude = 28.7041,
            longitude = 77.1025,
            address = "New Delhi"
        )

        coEvery { locationClient.getFreshLocation() } returns testLocation
        coEvery { locationClient.reverseGeocode(28.7041, 77.1025) } returns "New Delhi"
        coEvery { authRepository.currentUser } returns flowOf(testUser)
        coEvery { settingsRepository.isAutoSmsEnabled } returns flowOf(true)
        coEvery { contactRepository.getContactsSync() } returns testContacts
        coEvery { emergencyRepository.triggerSos(any(), "Ananya Singh") } returns Resource.Success(testIncident)

        val result = triggerSosUseCase()

        assertTrue(result is Resource.Success)
        assertEquals("inc-101", (result as Resource.Success).data.id)
        coVerify(exactly = 1) { emergencyRepository.triggerSos(any(), "Ananya Singh") }
        coVerify(exactly = 1) { emergencyRepository.broadcastEmergencySms(testContacts, any()) }
    }
}