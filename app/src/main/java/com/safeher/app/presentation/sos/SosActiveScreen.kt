package com.safeher.app.presentation.sos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeher.app.core.designsystem.CrimsonEmergency
import com.safeher.app.core.designsystem.PulsingDot
import com.safeher.app.core.designsystem.SafeGreen
import com.safeher.app.core.designsystem.SafeHerButton
import com.safeher.app.core.designsystem.SafeHerTopBar

@Composable
fun SosActiveScreen(
    incidentId: String,
    viewModel: SosViewModel,
    onSosFinished: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showStopDialog by remember { mutableStateOf(false) }
    var stopReason by remember { mutableStateOf("") }

    LaunchedEffect(incidentId) {
        viewModel.loadActiveSos(incidentId)
    }

    LaunchedEffect(state.isCancelled) {
        if (state.isCancelled) {
            onSosFinished()
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text("End Emergency SOS?", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please confirm that you are completely safe before stopping the emergency alert.")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = stopReason,
                        onValueChange = { stopReason = it },
                        label = { Text("Resolution Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showStopDialog = false
                        viewModel.stopSos(stopReason.ifBlank { "User marked safe" })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                ) {
                    Text("Confirm I Am Safe", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text("Keep Alert Active")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            SafeHerTopBar(
                title = "EMERGENCY SOS ACTIVE"
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Banner
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CrimsonEmergency.copy(alpha = 0.12f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(color = CrimsonEmergency)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "LIVE DISTRESS BROADCAST",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonEmergency
                        )
                    }

                    // Elapsed Timer
                    val minutes = state.elapsedSeconds / 60
                    val seconds = state.elapsedSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = CrimsonEmergency
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Direct Call 112 Card
            SafeHerButton(
                text = "Call Emergency Services (${state.emergencyNumber})",
                leadingIcon = Icons.Default.Call,
                containerColor = CrimsonEmergency,
                onClick = {
                    val callIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${state.emergencyNumber}")
                    }
                    context.startActivity(callIntent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Location Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = CrimsonEmergency
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Current Live Coordinates",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = state.liveLocation?.address ?: "Acquiring precision GPS...",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    if (state.liveLocation != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Lat: ${state.liveLocation!!.latitude}, Lng: ${state.liveLocation!!.longitude} (Acc: ±${state.liveLocation!!.accuracy.toInt()}m)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio Evidence Control Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Incident Audio Evidence",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (state.isRecordingAudio) "Recording background audio..." else "Audio recording paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.isRecordingAudio) CrimsonEmergency else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            if (state.isRecordingAudio) viewModel.stopAudioRecording() else viewModel.startAudioRecording()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.isRecordingAudio) CrimsonEmergency else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (state.isRecordingAudio) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Emergency Contacts Notified List
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notified Emergency Contacts (${state.contacts.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.contacts.isEmpty()) {
                        Text(
                            text = "No emergency contacts configured. Please add contacts in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonEmergency
                        )
                    } else {
                        state.contacts.forEach { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${contact.name} (${contact.relationship})",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = contact.phoneNumber,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SafeGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SMS Sent",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SafeGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stop SOS Button
            Button(
                onClick = { showStopDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "I AM SAFE (END SOS)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
