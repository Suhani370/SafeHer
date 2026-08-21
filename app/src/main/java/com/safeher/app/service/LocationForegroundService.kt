package com.safeher.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.notifications.NotificationHelper
import com.safeher.app.data.local.dao.LocationWaypointDao
import com.safeher.app.data.local.entity.LocationWaypointEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject lateinit var locationClient: LocationClient
    @Inject lateinit var waypointDao: LocationWaypointDao
    @Inject lateinit var notificationHelper: NotificationHelper

    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var trackingJob: Job? = null

    companion object {
        const val ACTION_START_SOS_TRACKING = "ACTION_START_SOS_TRACKING"
        const val ACTION_START_JOURNEY_TRACKING = "ACTION_START_JOURNEY_TRACKING"
        const val ACTION_STOP_TRACKING = "ACTION_STOP_TRACKING"
        const val EXTRA_INCIDENT_ID = "EXTRA_INCIDENT_ID"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_SOS_TRACKING -> {
                val incidentId = intent.getStringExtra(EXTRA_INCIDENT_ID) ?: "ACTIVE_SOS"
                startForeground(
                    NotificationHelper.NOTIFICATION_ID_LOCATION,
                    notificationHelper.buildSosForegroundNotification("Sharing real-time emergency coordinates with contacts")
                )
                startLocationTracking(incidentId, 5000L)
            }
            ACTION_START_JOURNEY_TRACKING -> {
                val journeyId = intent.getStringExtra(EXTRA_INCIDENT_ID) ?: "ACTIVE_JOURNEY"
                startForeground(
                    NotificationHelper.NOTIFICATION_ID_LOCATION,
                    notificationHelper.buildLocationForegroundNotification(
                        "Journey Monitoring Active",
                        "SafeHer is actively protecting your route"
                    )
                )
                startLocationTracking(journeyId, 10000L)
            }
            ACTION_STOP_TRACKING -> {
                stopLocationTracking()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startLocationTracking(incidentId: String, intervalMs: Long) {
        trackingJob?.cancel()
        trackingJob = locationClient.getLocationUpdates(intervalMs)
            .onEach { loc ->
                waypointDao.insertWaypoint(
                    LocationWaypointEntity(
                        incidentId = incidentId,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        accuracy = loc.accuracy,
                        timestamp = loc.timestamp,
                        isSynced = false
                    )
                )
            }
            .launchIn(serviceScope)
    }

    private fun stopLocationTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
