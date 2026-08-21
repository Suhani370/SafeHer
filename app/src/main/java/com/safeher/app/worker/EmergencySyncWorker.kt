package com.safeher.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.safeher.app.data.local.dao.IncidentDao
import com.safeher.app.data.local.dao.LocationWaypointDao
import com.safeher.app.data.local.dao.PendingSyncDao
import com.safeher.app.data.remote.firebase.FirebaseAuthSource
import com.safeher.app.data.remote.firebase.FirestoreEmergencySource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmergencySyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = com.safeher.app.SafeHerApp.database
        val firestore = FirestoreEmergencySource()
        val auth = FirebaseAuthSource()

        val uid = auth.currentUserId ?: return@withContext Result.retry()

        try {
            val incidentDao = database.incidentDao()
            val waypointDao = database.locationWaypointDao()

            // 1. Sync pending unsynced incidents
            val unsyncedIncidents = incidentDao.getUnsyncedIncidents()
            for (inc in unsyncedIncidents) {
                val res = firestore.uploadIncident(uid, inc)
                if (res is com.safeher.app.core.common.Resource.Success) {
                    incidentDao.updateIncident(inc.copy(isSynced = true))
                }
            }

            // 2. Sync pending unsynced waypoints
            val unsyncedWaypoints = waypointDao.getUnsyncedWaypoints()
            if (unsyncedWaypoints.isNotEmpty()) {
                val grouped = unsyncedWaypoints.groupBy { it.incidentId }
                for ((incId, points) in grouped) {
                    val res = firestore.uploadWaypoints(uid, incId, points)
                    if (res is com.safeher.app.core.common.Resource.Success) {
                        waypointDao.markWaypointsSynced(points.map { it.id })
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
