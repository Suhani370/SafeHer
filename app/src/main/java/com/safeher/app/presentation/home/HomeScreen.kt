package com.safeher.app.presentation.home

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.safeher.app.core.designsystem.AlertAmber
import com.safeher.app.core.designsystem.CrimsonEmergency
import com.safeher.app.core.designsystem.PrimaryPurple
import com.safeher.app.core.designsystem.SafeGreen
import com.safeher.app.core.designsystem.SafeHerTopBar
import com.safeher.app.core.location.LocationState
import com.safeher.app.core.permissions.PermissionManager
import com.safeher.app.domain.model.EmergencyStatus

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToSosActive: (String) -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToSafetyTimer: () -> Unit,
    onNavigateToJourney: () -> Unit,
    onNavigateToLiveLocation: () -> Unit,
    onNavigateToDiscreetMode: () -> Unit,
    onNavigateToNearbyHelp: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val isGranted = permissionsMap.values.any { it }
        viewModel.onPermissionResult(isGranted)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onResume()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    val emergencyStatus = if (state.activeIncident != null) {
        EmergencyStatus.ActiveSos(state.activeIncident!!.id, state.activeIncident!!.timestamp)
    } else {
        EmergencyStatus.Safe
    }

    Scaffold(
        topBar = {
            SafeHerTopBar(
                title = "SafeHer",
                actions = {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CrimsonEmergency),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable {
                                val callIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${state.emergencyNumber}")
                                }
                                context.startActivity(callIntent)
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Dial Emergency",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = state.emergencyNumber,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // System Status Card with Live LocationState Handling
            SystemStatusCard(
                isOnline = state.isOnline,
                locationState = state.locationState,
                contactsCount = state.contacts.size,
                batteryPercentage = state.batteryLevel,
                emergencyStatus = emergencyStatus,
                onRequestPermission = {
                    locationPermissionLauncher.launch(PermissionManager.locationPermissions)
                },
                onOpenLocationSettings = {
                    viewModel.openLocationSettings()
                },
                onRetryLocation = {
                    viewModel.startLocationMonitoring()
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Center Emergency SOS Button
            SosButton(
                onTriggerSos = {
                    viewModel.triggerSos(onSuccess = { incidentId ->
                        onNavigateToSosActive(incidentId)
                    })
                }
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Safety Tools Section
            Text(
                text = "Safety Protection Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // 2x2 Feature Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Safety Timer",
                    subtitle = "Auto-alert if safe arrival is not confirmed",
                    icon = Icons.Default.Timer,
                    accentColor = AlertAmber,
                    onClick = onNavigateToSafetyTimer,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = "Live Journey",
                    subtitle = "Real-time route & safety check-in",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    accentColor = SafeGreen,
                    onClick = onNavigateToJourney,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Emergency Contacts",
                    subtitle = "${state.contacts.size} configured",
                    icon = Icons.Default.Groups,
                    accentColor = PrimaryPurple,
                    onClick = onNavigateToContacts,
                    modifier = Modifier.weight(1f)
                )

                QuickActionCard(
                    title = "Discreet Mode",
                    subtitle = "Disguise app with decoy screen",
                    icon = Icons.Default.Lock,
                    accentColor = MaterialTheme.colorScheme.primary,
                    onClick = onNavigateToDiscreetMode,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}