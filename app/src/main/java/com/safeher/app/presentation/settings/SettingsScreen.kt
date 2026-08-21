package com.safeher.app.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeher.app.core.designsystem.SafeHerTopBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showNumberDialog by remember { mutableStateOf(false) }
    var tempEmergencyNumber by remember { mutableStateOf("") }

    if (showNumberDialog) {
        AlertDialog(
            onDismissRequest = { showNumberDialog = false },
            title = { Text("Configure Emergency Number") },
            text = {
                Column {
                    Text("Set the direct emergency dial number for your jurisdiction (e.g. 112 for India/EU, 911 for USA, 999 for UK).")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempEmergencyNumber,
                        onValueChange = { tempEmergencyNumber = it },
                        label = { Text("Emergency Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (tempEmergencyNumber.isNotBlank()) {
                            viewModel.setEmergencyNumber(tempEmergencyNumber)
                        }
                        showNumberDialog = false
                    }
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNumberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = { SafeHerTopBar(title = "Settings & Emergency Preferences", onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Emergency Response",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Primary Emergency Services Number", fontWeight = FontWeight.SemiBold)
                            Text("Currently set to ${state.emergencyNumber}", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = {
                            tempEmergencyNumber = state.emergencyNumber
                            showNumberDialog = true
                        }) {
                            Text("Change")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-Send Emergency SMS", fontWeight = FontWeight.SemiBold)
                            Text("Dispatch Google Maps coordinates automatically when SOS triggers", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.isAutoSmsEnabled,
                            onCheckedChange = { viewModel.setAutoSmsEnabled(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Audio Evidence Recording", fontWeight = FontWeight.SemiBold)
                            Text("Automatically capture background audio during active SOS", style = MaterialTheme.typography.bodySmall)
                        }
                        Switch(
                            checked = state.isAutoAudioRecordingEnabled,
                            onCheckedChange = { viewModel.setAutoAudioRecordingEnabled(it) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Privacy & Security",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data Sovereignty & Privacy",
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "SafeHer stores all sensitive contacts, safety timers, and incident evidence on-device in an encrypted Room database. Location tracking is active only during an explicit SOS or Journey session and is never tracked continuously in the background without user initiation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
