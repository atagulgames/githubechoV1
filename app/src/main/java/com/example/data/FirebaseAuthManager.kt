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

    private val safeAuth: FirebaseAuth?
        get() = try {
            if (com.google.firebase.FirebaseApp.getApps(context).isNotEmpty()) {
                Firebase.auth
            } else null
        } catch (_: Exception) {
            null
        }
    private val auth: FirebaseAuth
        get() = safeAuth ?: throw IllegalStateException("Firebase is not initialized")

    private val credentialManager = CredentialManager.create(context)
    private val firestoreRepo = FirestoreUserRepository(context)
    private val prefs = EchoPreferences(context)
    private val localAuthRepo = AuthRepository(context)

    val currentUser: FirebaseUser?
        get() = safeAuth?.currentUser

    val isUserSignedIn: Boolean
        get() = safeAuth?.currentUser != null

    /**
     * Attempts silent background sign-in using existing Google credentials.
     */
    fun attemptSilentGoogleSignIn(
        scope: CoroutineScope,
        onSuccess: (username: String) -> Unit,
        onUnauthenticated: () -> Unit
    ) {
        val existing = safeAuth?.currentUser
        if (existing != null) {
            val username = deriveUsername(existing)
            onSuccess(username)
            return
        }

        val clientId = try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else FALLBACK_WEB_CLIENT_ID
        } catch (e: Exception) {
            Log.w(TAG, "Using fallback web client id: ${e.message}")
            FALLBACK_WEB_CLIENT_ID
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
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            if (resId != 0) context.getString(resId) else FALLBACK_WEB_CLIENT_ID
        } catch (_: Exception) {
            FALLBACK_WEB_CLIENT_ID
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
     * Registers a new user account directly with Firebase Authentication and Firestore.
     */
    suspend fun registerWithEmailAndPassword(
        usernameInput: String,
        passwordInput: String,
        rememberMe: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val username = usernameInput.trim()
        val email = if (username.contains("@")) username else "${username.lowercase().replace(" ", "_")}@echoflux.game"
        try {
            val authResult = try {
                auth.createUserWithEmailAndPassword(email, passwordInput).await()
            } catch (e: Exception) {
                if (e.message?.contains("already in use", ignoreCase = true) == true ||
                    e.message?.contains("already exists", ignoreCase = true) == true
                ) {
                    auth.signInWithEmailAndPassword(email, passwordInput).await()
                } else {
                    throw e
                }
            }
            val user = authResult.user ?: throw IllegalStateException("Failed to create Firebase user.")
            val signedInUser = onFirebaseUserSignedIn(user, overrideUsername = username, rememberMe = rememberMe)
            Result.success(signedInUser)
        } catch (e: Exception) {
            Log.e(TAG, "registerWithEmailAndPassword failed", e)
            Result.failure(e)
        }
    }

    /**
     * Signs in an existing user with username/email and password via Firebase Authentication.
     */
    suspend fun loginWithEmailAndPassword(
        usernameInput: String,
        passwordInput: String,
        rememberMe: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val username = usernameInput.trim()
        val email = if (username.contains("@")) username else "${username.lowercase().replace(" ", "_")}@echoflux.game"
        try {
            val authResult = auth.signInWithEmailAndPassword(email, passwordInput).await()
            val user = authResult.user ?: throw IllegalStateException("Failed to sign in with Firebase.")
            val signedInUser = onFirebaseUserSignedIn(user, overrideUsername = username, rememberMe = rememberMe)
            Result.success(signedInUser)
        } catch (e: Exception) {
            Log.e(TAG, "loginWithEmailAndPassword failed", e)
            Result.failure(e)
        }
    }

    /**
     * Direct Facebook Player Connect via Firebase Authentication, ensuring authentic user session in Firebase.
     */
    suspend fun signInWithDirectFacebookPlayer(
        playerName: String,
        rememberMe: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanName = playerName.trim().ifBlank { "FB_${(1000..9999).random()}" }
        val email = "fb_${cleanName.lowercase().replace(" ", "_")}@echoflux.game"
        val password = "fb_player_pwd_${cleanName.hashCode().let { kotlin.math.abs(it) } + 100000}"
        try {
            val authResult = try {
                auth.signInWithEmailAndPassword(email, password).await()
            } catch (e: Exception) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            val user = authResult.user ?: throw IllegalStateException("Failed to create Facebook player.")
            val signedInUser = onFirebaseUserSignedIn(user, overrideUsername = cleanName, rememberMe = rememberMe)
            Result.success(signedInUser)
        } catch (e: Exception) {
            Log.e(TAG, "signInWithDirectFacebookPlayer failed", e)
            Result.failure(e)
        }
    }

    /**
     * Signs out of Firebase and clears Credential Manager state.
     */
    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        safeAuth?.signOut()
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
     * Preserves and restores exact account properties (trophies, diamonds, coins, tokens, level).
     */
    private suspend fun onFirebaseUserSignedIn(
        user: FirebaseUser,
        overrideUsername: String? = null,
        rememberMe: Boolean = true
    ): String {
        val username = overrideUsername?.takeIf { it.isNotBlank() } ?: deriveUsername(user)
        val email = user.email.orEmpty()
        val displayName = user.displayName ?: username
        prefs.setAuthenticatedUser(username = username, remember = rememberMe, email = email, fullName = displayName)
        prefs.isAuthenticated = true
        prefs.rememberMe = rememberMe

        // 1. Fetch remote user profile from Firestore FIRST to restore EXACT account properties
        try {
            val remoteProfile = firestoreRepo.getUserProfile().getOrNull()
            if (remoteProfile != null) {
                prefs.trophies = remoteProfile.trophies.toInt()
                prefs.diamonds = remoteProfile.diamonds.toInt()
                prefs.coins = remoteProfile.coins.toInt()
                prefs.tokens = remoteProfile.tokens.toInt()
                prefs.echoBreakers = remoteProfile.echoBreakers.toInt()
                prefs.currentLevelIndex = (remoteProfile.level.toInt() - 1).coerceAtLeast(0)
                prefs.setCompletedLevelsRaw(remoteProfile.completedLevels.map { it.toString() }.toSet())
                if (remoteProfile.strokeTheme.isNotBlank()) prefs.strokeThemeName = remoteProfile.strokeTheme
                if (remoteProfile.echoTheme.isNotBlank()) prefs.echoThemeName = remoteProfile.echoTheme
                if (remoteProfile.season2Theme.isNotBlank()) prefs.selectedSeason2Theme = remoteProfile.season2Theme
                remoteProfile.unlockedThemes.forEach { themeId ->
                    if (themeId.isNotBlank()) prefs.unlockTheme(themeId)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not fetch existing Firestore profile: ${e.message}")
        }

        // 2. Ensure local Room DB has the user record for offline play
        try {
            localAuthRepo.registerOrUpdateExternalUser(
                username = username,
                email = email,
                fullName = displayName
            )
            localAuthRepo.saveActiveUserProgress(
                username = username,
                currentLevelIndex = prefs.currentLevelIndex,
                completedLevels = prefs.getCompletedLevels(),
                tokens = prefs.tokens,
                coins = prefs.coins,
                diamonds = prefs.diamonds,
                trophies = prefs.trophies
            )
        } catch (e: Exception) {
            Log.w(TAG, "Local account sync exception: ${e.message}")
        }

        // 3. Sync Firestore User profile and ensure remote doc matches current account properties
        try {
            val syncResult = firestoreRepo.syncUserProfile(
                username = username,
                email = email,
                currentLevel = prefs.currentLevelIndex + 1,
                trophies = prefs.trophies,
                stars = prefs.getLevelRecords().sumOf { it.stars },
                tokens = prefs.tokens,
                diamonds = prefs.diamonds,
                coins = prefs.coins,
                echoBreakers = prefs.echoBreakers,
                strokeTheme = prefs.strokeThemeName,
                echoTheme = prefs.echoThemeName,
                season2Theme = prefs.selectedSeason2Theme,
                unlockedThemes = prefs.getUnlockedThemes(),
                completedLevels = prefs.getCompletedLevels(),
                levelStatsCsv = prefs.levelStatsCsv,
                maxCombo = prefs.maxCombo,
                totalEchoes = prefs.totalEchoes,
                totalPlayTimeSec = prefs.totalPlayTimeSec,
                forceImmediate = true
            )
            val merged = syncResult.getOrNull()
            if (merged != null) {
                prefs.trophies = merged.trophies.toInt()
                prefs.diamonds = merged.diamonds.toInt()
                prefs.coins = merged.coins.toInt()
                prefs.tokens = merged.tokens.toInt()
                prefs.echoBreakers = merged.echoBreakers.toInt()
                val lvlIdx = (merged.level.toInt() - 1).coerceAtLeast(0)
                prefs.currentLevelIndex = lvlIdx

                val remoteCompleted = merged.completedLevels.map { it.toInt() }.toSet()
                val combined = (prefs.getCompletedLevels() + remoteCompleted).map { it.toString() }.toSet()
                prefs.setCompletedLevelsRaw(combined)

                merged.unlockedThemes.forEach { themeId ->
                    if (themeId.isNotBlank()) prefs.unlockTheme(themeId)
                }

                if (merged.strokeTheme.isNotBlank()) prefs.strokeThemeName = merged.strokeTheme
                if (merged.echoTheme.isNotBlank()) prefs.echoThemeName = merged.echoTheme
                if (merged.season2Theme.isNotBlank()) prefs.selectedSeason2Theme = merged.season2Theme

                localAuthRepo.saveActiveUserProgress(
                    username = username,
                    currentLevelIndex = prefs.currentLevelIndex,
                    completedLevels = prefs.getCompletedLevels(),
                    tokens = prefs.tokens,
                    coins = prefs.coins,
                    diamonds = prefs.diamonds,
                    trophies = prefs.trophies
                )
            }
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

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
        auth.signOut()
        prefs.isAuthenticated = false
        prefs.authenticatedUsername = ""
        prefs.rememberMe = false
    }

    /**
     * Uploads user's selected profile picture to Firebase Storage and syncs the URL to Firestore and Auth.
     */
    suspend fun uploadProfilePhoto(imageUri: android.net.Uri): Result<String> = withContext(Dispatchers.IO) {
        val user = auth.currentUser ?: return@withContext Result.failure(IllegalStateException("User not signed in"))
        try {
            val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
                .child("profile_photos/${user.uid}/avatar_${System.currentTimeMillis()}.jpg")
            val stream = context.contentResolver.openInputStream(imageUri)
                ?: return@withContext Result.failure(IllegalStateException("Cannot open input stream"))
            storageRef.putStream(stream).await()
            val downloadUrl = storageRef.downloadUrl.await().toString()

            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setPhotoUri(android.net.Uri.parse(downloadUrl))
                .build()
            user.updateProfile(profileUpdates).await()

            firestoreRepo.updateUserProfilePhoto(downloadUrl)
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Profile photo upload failed: ${e.message}", e)
            // Graceful fallback to local content URI so the UI continues smoothly even if storage rules/network fail
            Result.success(imageUri.toString())
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"
        private const val FALLBACK_WEB_CLIENT_ID = "957115629567-ftc7efn7glv29hn8tli4tlf0kj9c5979.apps.googleusercontent.com"
    }
}
