package com.safeher.app

import android.app.Application
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.safeher.app.core.notifications.NotificationHelper
import com.safeher.app.data.local.SafeHerDatabase
import com.safeher.app.worker.EmergencySyncWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class SafeHerApp : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    companion object {
        lateinit var instance: SafeHerApp
            private set
        lateinit var database: SafeHerDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = Room.databaseBuilder(
            applicationContext,
            SafeHerDatabase::class.java,
            "safeher_database"
        ).fallbackToDestructiveMigration().build()

        notificationHelper.createNotificationChannels()
        schedulePeriodicEmergencySync()
    }

    private fun schedulePeriodicEmergencySync() {
        val syncConstraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<EmergencySyncWorker>(
            15, TimeUnit.MINUTES
        ).setConstraints(syncConstraints).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SafeHerEmergencySync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}
