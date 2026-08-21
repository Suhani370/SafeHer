package com.safeher.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.safeher.app.data.local.entity.EmergencyContactEntity
import com.safeher.app.data.local.entity.IncidentEntity
import com.safeher.app.data.local.entity.LocationWaypointEntity
import com.safeher.app.data.local.entity.PendingSyncEntity
import com.safeher.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfile(): Flow<UserEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfileSync(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateUser(user: UserEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearUser()
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, priorityOrder ASC, id ASC")
    fun getAllContacts(): Flow<List<EmergencyContactEntity>>

    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, priorityOrder ASC, id ASC")
    suspend fun getAllContactsSync(): List<EmergencyContactEntity>

    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): EmergencyContactEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: EmergencyContactEntity): Long

    @Update
    suspend fun updateContact(contact: EmergencyContactEntity)

    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)

    @Query("DELETE FROM emergency_contacts")
    suspend fun clearContacts()
}

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE status = 'ACTIVE' ORDER BY timestamp DESC LIMIT 1")
    fun getActiveEmergency(): Flow<IncidentEntity?>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: String): IncidentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Update
    suspend fun updateIncident(incident: IncidentEntity)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncidentById(id: String)

    @Query("SELECT * FROM incidents WHERE isSynced = 0")
    suspend fun getUnsyncedIncidents(): List<IncidentEntity>
}

@Dao
interface PendingSyncDao {
    @Query("SELECT * FROM pending_sync_queue ORDER BY createdAt ASC")
    suspend fun getAllPending(): List<PendingSyncEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueue(item: PendingSyncEntity): Long

    @Query("DELETE FROM pending_sync_queue WHERE id = :id")
    suspend fun remove(id: Long)

    @Query("UPDATE pending_sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: Long)
}

@Dao
interface LocationWaypointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaypoint(point: LocationWaypointEntity): Long

    @Query("SELECT * FROM location_waypoints WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    fun getWaypointsForIncident(incidentId: String): Flow<List<LocationWaypointEntity>>

    @Query("SELECT * FROM location_waypoints WHERE isSynced = 0 LIMIT 50")
    suspend fun getUnsyncedWaypoints(): List<LocationWaypointEntity>

    @Query("UPDATE location_waypoints SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markWaypointsSynced(ids: List<Long>)
}
