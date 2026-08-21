package com.safeher.app.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.safeher.app.core.common.Resource
import com.safeher.app.data.local.entity.EmergencyContactEntity
import com.safeher.app.data.local.entity.IncidentEntity
import com.safeher.app.data.local.entity.LocationWaypointEntity
import com.safeher.app.domain.model.UserProfile
import kotlinx.coroutines.tasks.await

class FirebaseAuthSource {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        null
    }

    val currentUserId: String?
        get() = auth?.currentUser?.uid

    suspend fun signIn(email: String, password: String): Resource<String> {
        return try {
            val client = auth ?: return Resource.Error("Firebase Auth is not configured on this device.")
            val result = client.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found.")
            Resource.Success(uid)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Authentication failed", e)
        }
    }

    suspend fun signUp(email: String, password: String): Resource<String> {
        return try {
            val client = auth ?: return Resource.Error("Firebase Auth is not configured on this device.")
            val result = client.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: return Resource.Error("User ID not found.")
            Resource.Success(uid)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration failed", e)
        }
    }

    suspend fun sendPasswordReset(email: String): Resource<Unit> {
        return try {
            val client = auth ?: return Resource.Error("Firebase Auth is not configured on this device.")
            client.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to send reset email", e)
        }
    }

    fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // Ignore
        }
    }
}

class FirestoreEmergencySource {
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (e: Exception) {
        null
    }

    suspend fun saveUserProfile(userId: String, profile: UserProfile): Resource<Unit> {
        return try {
            val db = firestore ?: return Resource.Error("Firestore is not configured.")
            val map = hashMapOf(
                "id" to profile.id,
                "fullName" to profile.fullName,
                "email" to profile.email,
                "phoneNumber" to profile.phoneNumber,
                "bloodGroup" to profile.bloodGroup,
                "emergencyNote" to profile.emergencyNote,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("users").document(userId).set(map, SetOptions.merge()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to sync profile with cloud", e)
        }
    }

    suspend fun uploadIncident(userId: String, incident: IncidentEntity): Resource<Unit> {
        return try {
            val db = firestore ?: return Resource.Error("Firestore is not configured.")
            val map = hashMapOf(
                "id" to incident.id,
                "type" to incident.type,
                "status" to incident.status,
                "timestamp" to incident.timestamp,
                "latitude" to incident.latitude,
                "longitude" to incident.longitude,
                "address" to incident.address,
                "notes" to incident.notes,
                "durationSeconds" to incident.durationSeconds,
                "contactsNotifiedCount" to incident.contactsNotifiedCount
            )
            db.collection("users")
                .document(userId)
                .collection("incidents")
                .document(incident.id)
                .set(map, SetOptions.merge())
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to upload incident to Firestore: ${e.message}", e)
        }
    }

    suspend fun uploadWaypoints(userId: String, incidentId: String, waypoints: List<LocationWaypointEntity>): Resource<Unit> {
        return try {
            val db = firestore ?: return Resource.Error("Firestore is not configured.")
            val batch = db.batch()
            val collection = db.collection("users")
                .document(userId)
                .collection("incidents")
                .document(incidentId)
                .collection("waypoints")

            for (wp in waypoints) {
                val doc = collection.document()
                val map = hashMapOf(
                    "latitude" to wp.latitude,
                    "longitude" to wp.longitude,
                    "accuracy" to wp.accuracy,
                    "timestamp" to wp.timestamp
                )
                batch.set(doc, map)
            }
            batch.commit().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Failed to sync location waypoints: ${e.message}", e)
        }
    }
}
