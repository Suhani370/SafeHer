package com.safeher.app.presentation.journey

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeher.app.core.designsystem.PulsingDot
import com.safeher.app.core.designsystem.SafeGreen
import com.safeher.app.core.designsystem.SafeHerButton
import com.safeher.app.core.designsystem.SafeHerTopBar

@Composable
fun JourneyMonitoringScreen(
    viewModel: JourneyViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var destination by remember { mutableStateOf("") }
    var arrivalMinutes by remember { mutableIntStateOf(20) }

    Scaffold(
        topBar = { SafeHerTopBar(title = "Journey Monitoring", onNavigateBack = onNavigateBack) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.activeJourney == null) {
                Text(
                    text = "Track Your Trip in Real-Time",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SafeHer runs a background monitoring service along your route, ensuring you reach your destination securely.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = destination,
                    onValueChange = { destination = it },
                    label = { Text("Where are you heading?") },
                    placeholder = { Text("e.g. Home, Hostel, Office") },
                    leadingIcon = { Icon(Icons.Default.Navigation, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = arrivalMinutes.toString(),
                    onValueChange = { arrivalMinutes = it.toIntOrNull() ?: 20 },
                    label = { Text("Estimated Travel Time (Minutes)") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                SafeHerButton(
                    text = "Start Live Journey",
                    leadingIcon = Icons.Default.Navigation,
                    enabled = destination.isNotBlank(),
                    onClick = { viewModel.startJourney(destination, arrivalMinutes) }
                )
            } else {
                // Active Journey Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SafeGreen.copy(alpha = 0.12f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PulsingDot(color = SafeGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "MONITORING ACTIVE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SafeGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Destination: ${state.activeJourney!!.destinationName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Expected Duration: ${state.activeJourney!!.estimatedArrivalMinutes} mins",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (state.currentLocation != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Current GPS: ${state.currentLocation!!.latitude}, ${state.currentLocation!!.longitude}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { viewModel.endJourney() },
                    colors = ButtonDefaults.buttonColors(containerColor = SafeGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reached Safely (End Journey)", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
