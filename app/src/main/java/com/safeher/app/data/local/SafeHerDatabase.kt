package com.safeher.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.safeher.app.data.local.dao.ContactDao
import com.safeher.app.data.local.dao.IncidentDao
import com.safeher.app.data.local.dao.LocationWaypointDao
import com.safeher.app.data.local.dao.PendingSyncDao
import com.safeher.app.data.local.dao.UserDao
import com.safeher.app.data.local.entity.EmergencyContactEntity
import com.safeher.app.data.local.entity.IncidentEntity
import com.safeher.app.data.local.entity.LocationWaypointEntity
import com.safeher.app.data.local.entity.PendingSyncEntity
import com.safeher.app.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        EmergencyContactEntity::class,
        IncidentEntity::class,
        PendingSyncEntity::class,
        LocationWaypointEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafeHerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contactDao(): ContactDao
    abstract fun incidentDao(): IncidentDao
    abstract fun pendingSyncDao(): PendingSyncDao
    abstract fun locationWaypointDao(): LocationWaypointDao
}
