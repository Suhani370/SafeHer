package com.safeher.app.presentation.safetytimer

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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeher.app.core.designsystem.AlertAmber
import com.safeher.app.core.designsystem.CrimsonEmergency
import com.safeher.app.core.designsystem.SafeGreen
import com.safeher.app.core.designsystem.SafeHerButton
import com.safeher.app.core.designsystem.SafeHerTopBar
import com.safeher.app.domain.model.TimerState

@Composable
fun SafetyTimerScreen(
    viewModel: SafetyTimerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSosActive: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedMinutes by remember { mutableIntStateOf(30) }
    var destination by remember { mutableStateOf("") }

    LaunchedEffect(state.escalatedIncidentId) {
        state.escalatedIncidentId?.let { incId ->
            onNavigateToSosActive(incId)
        }
    }

    // Grace Countdown Urgent Modal
    if (state.timer.state == TimerState.EXPIRED_COUNTDOWN) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = AlertAmber)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Safety Check-In", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Your safety timer has expired! Please confirm that you are safe.")
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Auto-Escalating to Emergency SOS in: ${state.timer.remainingSeconds}s",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonEmergency
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmSafe() },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen)
                ) {
                    Text("I AM SAFE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { viewModel.startTimer(10, "Extended timer") }
                ) {
                    Text("+10 Min Extension")
                }
            }
        )
    }

    Scaffold(
        topBar = { SafeHerTopBar(title = "Safety Timer", onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state.timer.state) {
                TimerState.IDLE, TimerState.RESOLVED_SAFE -> {
                    Text(
                        text = "Set a Journey Timer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "If you don't confirm safe arrival before time runs out, SafeHer automatically alerts your emergency contacts.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Preset Durations
                    Text(
                        text = "Select Duration",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(15, 30, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = selectedMinutes == mins,
                                onClick = { selectedMinutes = mins },
                                label = { Text("$mins mins") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Destination / Trip Note (Optional)") },
                        placeholder = { Text("e.g. Walking home from Metro") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    SafeHerButton(
                        text = "Start Safety Timer ($selectedMinutes min)",
                        leadingIcon = Icons.Default.Timer,
                        onClick = { viewModel.startTimer(selectedMinutes, destination) }
                    )
                }

                TimerState.RUNNING -> {
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.size(240.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val mins = state.timer.remainingSeconds / 60
                                val secs = state.timer.remainingSeconds % 60
                                Text(
                                    text = String.format("%02d:%02d", mins, secs),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 52.sp),
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "TIME REMAINING",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    if (state.timer.destinationName.isNotBlank()) {
                        Text(
                            text = "Destination: ${state.timer.destinationName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Button(
                        onClick = { viewModel.confirmSafe() },
                        colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("I Have Arrived Safely", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { viewModel.resetTimer() }) {
                        Text("Cancel Timer")
                    }
                }

                else -> {}
            }
        }
    }
}
