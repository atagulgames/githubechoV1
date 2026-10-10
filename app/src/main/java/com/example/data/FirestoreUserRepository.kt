package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class FirestoreUser(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val email: String = "",
    val trophies: Long = 0L,
    val level: Long = 1L,
    val stars: Long = 0L,
    val tokens: Long = 10L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class FirestoreUserRepository(private val db: FirebaseFirestore) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = FirebaseAuth.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing Firestore.")
    }

    suspend fun syncUserProfile(
        username: String,
        email: String,
        currentLevel: Int,
        trophies: Int,
        stars: Int,
        tokens: Int
    ): Result<FirestoreUser> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                val existing = snapshot.toObject(FirestoreUser::class.java)
                val updates = mutableMapOf<String, Any>(
                    "username" to username.ifBlank { existing?.username ?: "Player" },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (email.isNotBlank()) updates["email"] = email
                if (trophies > (existing?.trophies ?: 0L)) updates["trophies"] = trophies.toLong()
                if (currentLevel > (existing?.level ?: 1L)) updates["level"] = currentLevel.toLong()
                if (stars > (existing?.stars ?: 0L)) updates["stars"] = stars.toLong()

                docRef.update(updates).await()
                val updatedSnap = docRef.get().await()
                Result.success(updatedSnap.toObject(FirestoreUser::class.java) ?: FirestoreUser(userId = uid, username = username))
            } else {
                val newProfile = hashMapOf<String, Any>(
                    "userId" to uid,
                    "username" to username.ifBlank { "Player_${uid.take(5)}" },
                    "email" to email,
                    "trophies" to trophies.toLong(),
                    "level" to currentLevel.toLong().coerceAtLeast(1L),
                    "stars" to stars.toLong(),
                    "tokens" to tokens.toLong(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(newProfile).await()
                val createdSnap = docRef.get().await()
                Result.success(createdSnap.toObject(FirestoreUser::class.java) ?: FirestoreUser(userId = uid, username = username))
            }
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<FirestoreUser?> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid)
        return try {
            val snapshot = docRef.get().await()
            Result.success(snapshot.toObject(FirestoreUser::class.java))
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }
}
