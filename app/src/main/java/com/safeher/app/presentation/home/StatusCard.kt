package com.safeher.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SignalCellularOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.safeher.app.core.designsystem.AlertAmber
import com.safeher.app.core.designsystem.CrimsonEmergency
import com.safeher.app.core.designsystem.PulsingDot
import com.safeher.app.core.designsystem.SafeGreen
import com.safeher.app.core.location.LocationState
import com.safeher.app.domain.model.EmergencyStatus

@Composable
fun SystemStatusCard(
    isOnline: Boolean,
    locationState: LocationState,
    contactsCount: Int,
    batteryPercentage: Int,
    emergencyStatus: EmergencyStatus,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRetryLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (emergencyStatus) {
                is EmergencyStatus.ActiveSos -> CrimsonEmergency.copy(alpha = 0.12f)
                is EmergencyStatus.TimerWarning -> AlertAmber.copy(alpha = 0.12f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Safety Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    when (emergencyStatus) {
                        is EmergencyStatus.ActiveSos -> {
                            PulsingDot(color = CrimsonEmergency)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY SOS ACTIVE",
                                style = MaterialTheme.typography.titleMedium,
                                color = CrimsonEmergency,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is EmergencyStatus.TimerWarning -> {
                            PulsingDot(color = AlertAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SAFETY TIMER WARNING",
                                style = MaterialTheme.typography.titleMedium,
                                color = AlertAmber,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        is EmergencyStatus.Safe -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PROTECTION READY",
                                style = MaterialTheme.typography.titleMedium,
                                color = SafeGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = if (contactsCount > 0) "$contactsCount Contacts" else "No Contacts",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (contactsCount > 0) MaterialTheme.colorScheme.onSurfaceVariant else CrimsonEmergency
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Location Content depending on LocationState
            when (locationState) {
                is LocationState.PermissionRequired -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AlertAmber,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Location permission is required for emergency protection.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onRequestPermission,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationSearching, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Grant Location Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is LocationState.LocationDisabled -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsOff,
                                contentDescription = null,
                                tint = CrimsonEmergency,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Device GPS / Location Services is disabled.",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onOpenLocationSettings,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonEmergency),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enable Location in Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                is LocationState.Locating -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PulsingDot(color = AlertAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Acquiring real GPS coordinates...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                is LocationState.Available -> {
                    val loc = locationState.location
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NearMe,
                                contentDescription = null,
                                tint = SafeGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = loc.address ?: "Current Location",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Coordinates: ${loc.getFormattedCoordinates()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is LocationState.Error -> {
                    Column {
                        Text(
                            text = "Location error: ${locationState.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = CrimsonEmergency
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedButton(
                            onClick = onRetryLocation,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Retry", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Indicator Badges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val (locIcon, locText, locColor) = when (locationState) {
                    is LocationState.Available -> Triple(Icons.Default.GpsFixed, "Location Active", SafeGreen)
                    is LocationState.Locating -> Triple(Icons.Default.GpsNotFixed, "Locating...", AlertAmber)
                    is LocationState.LocationDisabled -> Triple(Icons.Default.GpsOff, "GPS Off", CrimsonEmergency)
                    is LocationState.PermissionRequired -> Triple(Icons.Default.Warning, "Permission Needed", AlertAmber)
                    is LocationState.Error -> Triple(Icons.Default.Warning, "Location Error", CrimsonEmergency)
                }

                StatusIndicator(
                    icon = locIcon,
                    text = locText,
                    color = locColor
                )

                StatusIndicator(
                    icon = if (isOnline) Icons.Default.SignalCellularAlt else Icons.Default.SignalCellularOff,
                    text = if (isOnline) "Online" else "Offline / SMS Mode",
                    color = if (isOnline) SafeGreen else AlertAmber
                )

                StatusIndicator(
                    icon = Icons.Default.BatteryChargingFull,
                    text = "$batteryPercentage%",
                    color = if (batteryPercentage > 20) SafeGreen else AlertAmber
                )
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}