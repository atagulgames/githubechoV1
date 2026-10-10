package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * FirebaseAuthManager coordinates Firebase Authentication (Google Sign-In via Credential Manager
 * and Facebook Sign-In via Firebase OAuthProvider) and synchronizes user profiles with Firestore.
 */
class FirebaseAuthManager(private val context: Context) {

    private val auth: FirebaseAuth get() = Firebase.auth
    private val credentialManager = CredentialManager.create(context)
    private val firestoreRepo = FirestoreUserRepository(context)
    private val prefs = EchoPreferences(context)
    private val localAuthRepo = AuthRepository(context)

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Attempts silent background sign-in using existing Google credentials.
     */
    fun attemptSilentGoogleSignIn(
        scope: CoroutineScope,
        onSuccess: (username: String) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val existing = auth.currentUser
        if (existing != null) {
            val username = deriveUsername(existing)
            onSuccess(username)
            return
        }

        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.w(TAG, "default_web_client_id not found: ${e.message}")
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val username = onFirebaseUserSignedIn(user)
                        withContext(Dispatchers.Main) { onSuccess(username) }
                    } else {
                        withContext(Dispatchers.Main) { onUnauthenticated() }
                    }
                } else {
                    withContext(Dispatchers.Main) { onUnauthenticated() }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onUnauthenticated() }
            }
        }
    }

    /**
     * Interactive Google Sign-In with bottom sheet dialog via Jetpack Credential Manager.
     */
    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (username: String) -> Unit,
        onError: (message: String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val clientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            val err = "Google Sign-In konfigürasyonu eksik (default_web_client_id)."
            Log.e(TAG, err, e)
            onError(err)
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        val username = onFirebaseUserSignedIn(user)
                        withContext(Dispatchers.Main) { onSuccess(username) }
                    } else {
                        withContext(Dispatchers.Main) { onError("Could not retrieve user account.") }
                    }
                } else {
                    withContext(Dispatchers.Main) { onError("Unrecognized credential type.") }
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w(TAG, "Google Sign-In cancelled: ${e.message}", e)
                withContext(Dispatchers.Main) { onCancelled() }
            } catch (e: Exception) {
                Log.e(TAG, "Google Sign-In failed", e)
                val msg = e.localizedMessage ?: "An error occurred while signing in with Google."
                withContext(Dispatchers.Main) { onError(msg) }
            }
        }
    }

    /**
     * Interactive Facebook Sign-In via Firebase OAuthProvider with browser popup/custom tab.
     */
    fun signInWithFacebook(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (username: String) -> Unit,
        onError: (message: String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val provider = OAuthProvider.newBuilder("facebook.com")
        provider.addCustomParameter("display", "popup")
        provider.scopes = listOf("email", "public_profile")

        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    scope.launch {
                        val username = onFirebaseUserSignedIn(user)
                        withContext(Dispatchers.Main) { onSuccess(username) }
                    }
                }
            }.addOnFailureListener { e ->
                Log.e(TAG, "Facebook pending auth failed", e)
                onError(formatFacebookError(e))
            }
            return
        }

        auth.startActivityForSignInWithProvider(activity, provider.build())
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    scope.launch {
                        val username = onFirebaseUserSignedIn(user)
                        withContext(Dispatchers.Main) { onSuccess(username) }
                    }
                } else {
                    onError("Could not verify Facebook user.")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Facebook sign-in failed", e)
                if (e.message?.contains("canceled", ignoreCase = true) == true ||
                    e.message?.contains("cancelled", ignoreCase = true) == true) {
                    onCancelled()
                } else {
                    onError(formatFacebookError(e))
                }
            }
    }

    private fun formatFacebookError(e: Exception): String {
        val raw = e.localizedMessage ?: ""
        return if (raw.contains("configuration", ignoreCase = true) ||
            raw.contains("not found", ignoreCase = true) ||
            raw.contains("invalid-app-id", ignoreCase = true)
        ) {
            "Facebook Sign-In requires configuring the Facebook provider (App ID & Secret) in the Firebase Console. Please use 'Sign in with Google' instead!"
        } else {
            "Facebook sign-in failed: $raw"
        }
    }

    /**
     * Signs out of Firebase and clears Credential Manager state.
     */
    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        auth.signOut()
        scope.launch {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e(TAG, "Failed to clear credential state", e)
            } finally {
                withContext(Dispatchers.Main) { onComplete() }
            }
        }
    }

    /**
     * Synchronizes authenticated Firebase user into local preferences and Firestore database.
     */
    private suspend fun onFirebaseUserSignedIn(user: FirebaseUser): String {
        val username = deriveUsername(user)
        val email = user.email.orEmpty()
        val displayName = user.displayName ?: username
        prefs.setAuthenticatedUser(username = username, remember = true, email = email, fullName = displayName)
        prefs.isAuthenticated = true

        // Ensure local Room DB has the user record for offline play
        try {
            localAuthRepo.registerOrUpdateExternalUser(
                username = username,
                email = email,
                fullName = displayName
            )
        } catch (e: Exception) {
            Log.w(TAG, "Local account sync exception: ${e.message}")
        }

        // Sync Firestore User profile
        try {
            firestoreRepo.syncUserProfile(
                username = username,
                email = email,
                currentLevel = prefs.currentLevelIndex + 1,
                trophies = prefs.trophies,
                stars = prefs.getLevelRecords().sumOf { it.stars },
                tokens = prefs.tokens
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firestore sync failed: ${e.message}")
        }

        return username
    }

    private fun deriveUsername(user: FirebaseUser): String {
        return user.displayName?.takeIf { it.isNotBlank() }
            ?: user.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
            ?: "Player_${user.uid.take(6)}"
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
    }
}
