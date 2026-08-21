package com.safeher.app.di

import android.content.Context
import androidx.room.Room
import com.google.android.gms.location.LocationServices
import com.safeher.app.core.audio.AudioRecorderManager
import com.safeher.app.core.common.DefaultDispatcherProvider
import com.safeher.app.core.common.DispatcherProvider
import com.safeher.app.core.common.LiveNetworkMonitor
import com.safeher.app.core.common.NetworkMonitor
import com.safeher.app.core.location.DefaultLocationClient
import com.safeher.app.core.location.LocationClient
import com.safeher.app.core.notifications.NotificationHelper
import com.safeher.app.data.local.SafeHerDatabase
import com.safeher.app.data.local.dao.ContactDao
import com.safeher.app.data.local.dao.IncidentDao
import com.safeher.app.data.local.dao.LocationWaypointDao
import com.safeher.app.data.local.dao.PendingSyncDao
import com.safeher.app.data.local.dao.UserDao
import com.safeher.app.data.local.preferences.UserPreferencesDataStore
import com.safeher.app.data.remote.firebase.FirebaseAuthSource
import com.safeher.app.data.remote.firebase.FirestoreEmergencySource
import com.safeher.app.data.remote.places.NearbyHelpDataSource
import com.safeher.app.data.repository.AuthRepositoryImpl
import com.safeher.app.data.repository.ContactRepositoryImpl
import com.safeher.app.data.repository.EmergencyRepositoryImpl
import com.safeher.app.data.repository.IncidentRepositoryImpl
import com.safeher.app.data.repository.NearbyRepositoryImpl
import com.safeher.app.data.repository.SettingsRepositoryImpl
import com.safeher.app.domain.repository.AuthRepository
import com.safeher.app.domain.repository.ContactRepository
import com.safeher.app.domain.repository.EmergencyRepository
import com.safeher.app.domain.repository.IncidentRepository
import com.safeher.app.domain.repository.NearbyRepository
import com.safeher.app.domain.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDispatcherProvider(): DispatcherProvider = DefaultDispatcherProvider()

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor =
        LiveNetworkMonitor(context)

    @Provides
    @Singleton
    fun provideNotificationHelper(@ApplicationContext context: Context): NotificationHelper =
        NotificationHelper(context)

    @Provides
    @Singleton
    fun provideAudioRecorderManager(@ApplicationContext context: Context): AudioRecorderManager =
        AudioRecorderManager(context)

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(@ApplicationContext context: Context): UserPreferencesDataStore =
        UserPreferencesDataStore(context)

    @Provides
    @Singleton
    fun provideFirebaseAuthSource(): FirebaseAuthSource = FirebaseAuthSource()

    @Provides
    @Singleton
    fun provideFirestoreEmergencySource(): FirestoreEmergencySource = FirestoreEmergencySource()

    @Provides
    @Singleton
    fun provideNearbyHelpDataSource(): NearbyHelpDataSource = NearbyHelpDataSource()
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SafeHerDatabase {
        return Room.databaseBuilder(
            context,
            SafeHerDatabase::class.java,
            "safeher_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideUserDao(db: SafeHerDatabase): UserDao = db.userDao()

    @Provides
    fun provideContactDao(db: SafeHerDatabase): ContactDao = db.contactDao()

    @Provides
    fun provideIncidentDao(db: SafeHerDatabase): IncidentDao = db.incidentDao()

    @Provides
    fun providePendingSyncDao(db: SafeHerDatabase): PendingSyncDao = db.pendingSyncDao()

    @Provides
    fun provideLocationWaypointDao(db: SafeHerDatabase): LocationWaypointDao = db.locationWaypointDao()
}

@Module
@InstallIn(SingletonComponent::class)
object LocationModule {

    @Provides
    @Singleton
    fun provideLocationClient(@ApplicationContext context: Context): LocationClient {
        val client = LocationServices.getFusedLocationProviderClient(context)
        return DefaultLocationClient(context, client)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        userDao: UserDao,
        firebaseAuth: FirebaseAuthSource,
        firestoreSource: FirestoreEmergencySource,
        preferences: UserPreferencesDataStore
    ): AuthRepository = AuthRepositoryImpl(userDao, firebaseAuth, firestoreSource, preferences)

    @Provides
    @Singleton
    fun provideContactRepository(
        @ApplicationContext context: Context,
        contactDao: ContactDao
    ): ContactRepository = ContactRepositoryImpl(context, contactDao)

    @Provides
    @Singleton
    fun provideEmergencyRepository(
        @ApplicationContext context: Context,
        incidentDao: IncidentDao,
        contactDao: ContactDao,
        pendingSyncDao: PendingSyncDao,
        firestoreSource: FirestoreEmergencySource,
        firebaseAuth: FirebaseAuthSource
    ): EmergencyRepository = EmergencyRepositoryImpl(
        context,
        incidentDao,
        contactDao,
        pendingSyncDao,
        firestoreSource,
        firebaseAuth
    )

    @Provides
    @Singleton
    fun provideIncidentRepository(
        incidentDao: IncidentDao
    ): IncidentRepository = IncidentRepositoryImpl(incidentDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(
        preferences: UserPreferencesDataStore
    ): SettingsRepository = SettingsRepositoryImpl(preferences)

    @Provides
    @Singleton
    fun provideNearbyRepository(
        dataSource: NearbyHelpDataSource
    ): NearbyRepository = NearbyRepositoryImpl(dataSource)
}
