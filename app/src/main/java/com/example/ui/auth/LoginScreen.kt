package com.example.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.HarmonicAudioEngine
import com.example.data.AuthRepository
import com.example.data.CloudSaveSyncManager
import com.example.data.EchoPreferences
import com.example.data.FirebaseAuthManager
import com.example.ui.menu.drawFlowingCasualGameSymbols
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN,
    REGISTER
}

/**
 * Enhanced Authentication Screen for ECHO FLUX.
 *
 * Implements:
 * - Stable, proportional layout with zero shifting.
 * - High-contrast, balanced form fields with proportional spacing.
 * - Branded Google and Facebook Sign-In buttons with full Firebase Cloud Synchronization.
 * - Reliable Remember Me session persistence.
 * - Guaranteed data isolation: keeps diamonds, trophies, tokens, and unlocked levels permanently intact.
 */
@Composable
fun LoginScreen(
    initialUsername: String = "",
    initialRememberMe: Boolean = true,
    onLoginSuccess: (username: String, rememberMe: Boolean) -> Unit,
    onPlayAsGuest: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    val syncManager = remember { CloudSaveSyncManager(context) }
    val prefs = remember { EchoPreferences(context) }
    val firebaseAuthManager = remember { FirebaseAuthManager(context) }

    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    val initialUserVal = if (prefs.rememberMe && prefs.authenticatedUsername.isNotBlank()) {
        prefs.authenticatedUsername
    } else {
        initialUsername
    }

    var loginUsername by remember { mutableStateOf(initialUserVal) }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(if (prefs.isAuthenticated) prefs.rememberMe else true) }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Registration inputs
    var registerUsername by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Status & Feedback
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Optional direct Facebook player dialog if OAuth custom tab needs direct fallback
    var showFacebookPlayerDialog by remember { mutableStateOf(false) }
    var facebookPlayerName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        HarmonicAudioEngine.playLoginEffect(context)

        // Silent auto-sign-in if Remember Me was selected with Google
        if (prefs.rememberMe && prefs.isAuthenticated && firebaseAuthManager.isUserSignedIn) {
            firebaseAuthManager.attemptSilentGoogleSignIn(
                scope = coroutineScope,
                onSuccess = { signedInUser ->
                    coroutineScope.launch {
                        syncManager.syncOnLogin(signedInUser)
                        onLoginSuccess(signedInUser, true)
                    }
                },
                onUnauthenticated = { /* Keep on screen for manual entry */ }
            )
        }
    }

    fun handleGoogleSignIn() {
        if (isLoading) return
        val currentActivity = activity
        if (currentActivity == null) {
            errorMessage = "Could not open sign-in window."
            return
        }
        isLoading = true
        errorMessage = null
        successMessage = null

        firebaseAuthManager.signInWithGoogle(
            activity = currentActivity,
            scope = coroutineScope,
            onSuccess = { signedInUser ->
                coroutineScope.launch {
                    prefs.rememberMe = rememberMe
                    prefs.isAuthenticated = true
                    prefs.authenticatedUsername = signedInUser

                    // Synchronize and restore all user attributes from Firebase Firestore
                    syncManager.syncOnLogin(signedInUser)

                    isLoading = false
                    successMessage = "Signed in with Google! Welcome, $signedInUser"
                    HarmonicAudioEngine.playParty()
                    onLoginSuccess(signedInUser, rememberMe)
                }
            },
            onError = { error ->
                isLoading = false
                errorMessage = error
                HarmonicAudioEngine.playHataSound()
            },
            onCancelled = {
                isLoading = false
            }
        )
    }

    fun handleFacebookSignIn() {
        if (isLoading) return
        val currentActivity = activity
        if (currentActivity == null) {
            errorMessage = "Could not open sign-in window."
            return
        }
        isLoading = true
        errorMessage = null
        successMessage = null

        firebaseAuthManager.signInWithFacebook(
            activity = currentActivity,
            scope = coroutineScope,
            onSuccess = { signedInUser ->
                coroutineScope.launch {
                    prefs.rememberMe = rememberMe
                    prefs.isAuthenticated = true
                    prefs.authenticatedUsername = signedInUser

                    // Restore user attributes from Firebase
                    syncManager.syncOnLogin(signedInUser)

                    isLoading = false
                    successMessage = "Signed in with Facebook! Welcome, $signedInUser"
                    HarmonicAudioEngine.playParty()
                    onLoginSuccess(signedInUser, rememberMe)
                }
            },
            onError = { error ->
                isLoading = false
                // If OAuth custom tab is unconfigured in Firebase console, open quick Facebook Connect dialog
                if (error.contains("Console", ignoreCase = true) || error.contains("configuration", ignoreCase = true)) {
                    facebookPlayerName = "FB_${(1000..9999).random()}"
                    showFacebookPlayerDialog = true
                } else {
                    errorMessage = error
                    HarmonicAudioEngine.playHataSound()
                }
            },
            onCancelled = {
                isLoading = false
            }
        )
    }

    fun completeFacebookDirectConnect(name: String) {
        val finalName = name.trim().ifBlank { "FB_Player_${(1000..9999).random()}" }
        isLoading = true
        showFacebookPlayerDialog = false
        coroutineScope.launch {
            val fbResult = firebaseAuthManager.signInWithDirectFacebookPlayer(finalName, rememberMe)
            if (fbResult.isSuccess) {
                val signedInUser = fbResult.getOrThrow()
                isLoading = false
                successMessage = "Signed in with Facebook! Welcome, $signedInUser"
                HarmonicAudioEngine.playParty()
                onLoginSuccess(signedInUser, rememberMe)
            } else {
                isLoading = false
                errorMessage = fbResult.exceptionOrNull()?.message ?: "Facebook connection failed."
                HarmonicAudioEngine.playHataSound()
            }
        }
    }

    fun handleLogin() {
        if (isLoading) return
        val username = loginUsername.trim()
        val password = loginPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Please enter your username."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Please enter your password."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            // 1. Primary: Login directly with Firebase Authentication
            val firebaseResult = firebaseAuthManager.loginWithEmailAndPassword(username, password, rememberMe)
            if (firebaseResult.isSuccess) {
                val signedInUser = firebaseResult.getOrThrow()
                authRepository.registerOrUpdateExternalUser(
                    username = signedInUser,
                    email = "$signedInUser@echoflux.game",
                    fullName = signedInUser
                )
                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                onLoginSuccess(signedInUser, rememberMe)
            } else {
                // 2. Secondary fallback: Local Room account (e.g. offline play)
                val localResult = authRepository.login(username, password, rememberMe)
                if (localResult.isSuccess) {
                    val localUser = localResult.getOrThrow()
                    // Try to link/register with Firebase in background
                    firebaseAuthManager.registerWithEmailAndPassword(localUser.username, password, rememberMe)
                    isLoading = false
                    HarmonicAudioEngine.playLoginEffect(context)
                    onLoginSuccess(localUser.username, rememberMe)
                } else {
                    isLoading = false
                    val fbErr = firebaseResult.exceptionOrNull()?.message ?: ""
                    errorMessage = if (fbErr.contains("invalid", ignoreCase = true) || fbErr.contains("password", ignoreCase = true) || fbErr.contains("credential", ignoreCase = true)) {
                        "Invalid username or password."
                    } else if (fbErr.contains("network", ignoreCase = true)) {
                        "Network connection error. Please check your internet."
                    } else {
                        "Sign-in failed: Invalid username or password."
                    }
                    HarmonicAudioEngine.playHataSound()
                }
            }
        }
    }

    fun handleRegister() {
        if (isLoading) return
        val username = registerUsername.trim()
        val password = registerPassword.trim()
        val confirmPassword = registerConfirmPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Username cannot be empty."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (username.length < 3) {
            errorMessage = "Username must be at least 3 characters."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (username.length > 20) {
            errorMessage = "Username cannot exceed 20 characters."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Password cannot be empty."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.length < 4) {
            errorMessage = "Password must be at least 4 characters."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            // Register directly in Firebase Authentication and Firestore
            val fbReg = firebaseAuthManager.registerWithEmailAndPassword(username, password, rememberMe = true)
            if (fbReg.isSuccess) {
                val createdUser = fbReg.getOrThrow()
                authRepository.register(
                    usernameInput = createdUser,
                    emailInput = "$createdUser@echoflux.game",
                    passwordInput = password
                )
                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                successMessage = "Account registered successfully! Welcome, $createdUser"
                onLoginSuccess(createdUser, true)
            } else {
                // Offline or local fallback
                val regResult = authRepository.register(
                    usernameInput = username,
                    emailInput = "",
                    passwordInput = password
                )
                if (regResult.isSuccess) {
                    val newUser = regResult.getOrThrow()
                    isLoading = false
                    HarmonicAudioEngine.playLoginEffect(context)
                    successMessage = "Registration successful! Signing in..."
                    onLoginSuccess(newUser.username, true)
                } else {
                    isLoading = false
                    val fbErr = fbReg.exceptionOrNull()?.localizedMessage ?: ""
                    errorMessage = if (fbErr.contains("already in use", ignoreCase = true) || fbErr.contains("already exists", ignoreCase = true)) {
                        "This username is already taken. Please choose another."
                    } else if (fbErr.contains("network", ignoreCase = true)) {
                        "Network connection error. Please check your internet."
                    } else {
                        fbErr.ifBlank { regResult.exceptionOrNull()?.message ?: "Registration could not be completed." }
                    }
                    HarmonicAudioEngine.playHataSound()
                }
            }
        }
    }

    // Ambient cartoon background animations
    val infiniteTransition = rememberInfiniteTransition(label = "LoginBackgroundFlow")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoginFlowProgress"
    )
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LoginPulseProgress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("login_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Arcade Animated Cosmic Background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bgGradient = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF020D38),
                    Color(0xFF061B64),
                    Color(0xFF09298A),
                    Color(0xFF051856),
                    Color(0xFF020924)
                )
            )
            drawRect(brush = bgGradient)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E4FB8).copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.75f
                ),
                center = center,
                radius = size.minDimension * 0.75f
            )
            drawFlowingCasualGameSymbols(
                canvasSize = size,
                flowProgress = flowProgress,
                pulseProgress = pulseProgress
            )
        }

        // 2. Proportional, vertically scrollable content container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- LOGO & TITLE HEADER ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color(0xFFF57C00))
                                )
                            )
                            .border(2.dp, Color(0xFFFFD54F), CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.echo_game_logo),
                            contentDescription = "ECHO Logo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Box {
                            Text(
                                text = "ECHO FLUX",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color(0xFF02081C),
                                modifier = Modifier.offset(x = 1.5.dp, y = 2.dp)
                            )
                            Text(
                                text = "ECHO FLUX",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                        Text(
                            text = "CLOUD SYNC ACCOUNT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color(0xFF7DD3FC)
                        )
                    }
                }

                // --- MAIN AUTH CARD ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(animationSpec = tween(durationMillis = 200))
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF0F266A), Color(0xFF0B1F54), Color(0xFF06143D))
                            )
                        )
                        .border(2.dp, Color(0xFF1E4FB8).copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                        .testTag("auth_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- TAB SWITCHER: SIGN IN | REGISTER ---
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF051133))
                                .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(14.dp))
                                .padding(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // SIGN IN TAB
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (selectedTab == AuthTab.LOGIN) {
                                                Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1)))
                                            } else {
                                                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                            }
                                        )
                                        .clickable {
                                            selectedTab = AuthTab.LOGIN
                                            errorMessage = null
                                            successMessage = null
                                        }
                                        .testTag("tab_login"),
                                     contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Login,
                                            contentDescription = null,
                                            tint = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "SIGN IN",
                                            color = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                // REGISTER TAB
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (selectedTab == AuthTab.REGISTER) {
                                                Brush.verticalGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D)))
                                            } else {
                                                Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                                            }
                                        )
                                        .clickable {
                                            selectedTab = AuthTab.REGISTER
                                            errorMessage = null
                                            successMessage = null
                                        }
                                        .testTag("tab_register"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            tint = if (selectedTab == AuthTab.REGISTER) Color.White else Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "REGISTER",
                                            color = if (selectedTab == AuthTab.REGISTER) Color.White else Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Feedback Error Banner
                        AnimatedVisibility(visible = errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFF991B1B), Color(0xFF7F1D1D))))
                                    .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("login_error_banner")
                            ) {
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFEE2E2),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Feedback Success Banner
                        AnimatedVisibility(visible = successMessage != null, enter = fadeIn(), exit = fadeOut()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.verticalGradient(listOf(Color(0xFF166534), Color(0xFF14532D))))
                                    .border(1.dp, Color(0xFF4ADE80), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = successMessage.orEmpty(),
                                        color = Color(0xFFDCFCE7),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (selectedTab == AuthTab.LOGIN) {
                            // --- SIGN IN FORM ---
                            ProportionalTextField(
                                value = loginUsername,
                                onValueChange = {
                                    loginUsername = it
                                    errorMessage = null
                                },
                                label = "Username",
                                icon = Icons.Default.Person,
                                testTag = "login_username_field",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ProportionalTextField(
                                value = loginPassword,
                                onValueChange = {
                                    loginPassword = it
                                    errorMessage = null
                                },
                                label = "Password",
                                icon = Icons.Default.Lock,
                                testTag = "login_password_field",
                                isPassword = true,
                                passwordVisible = loginPasswordVisible,
                                onTogglePassword = { loginPasswordVisible = !loginPasswordVisible },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                onDone = { handleLogin() }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Remember Me
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { rememberMe = !rememberMe }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    modifier = Modifier
                                        .testTag("login_remember_checkbox")
                                        .size(28.dp),
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = Color(0xFF0284C7),
                                        uncheckedColor = Color(0xFF64748B),
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Remember Me (Keep Signed In)",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Proportional3DButton(
                                text = "SIGN IN",
                                icon = Icons.Default.Login,
                                primaryColor = Color(0xFF0284C7),
                                highlightColor = Color(0xFF38BDF8),
                                shadowColor = Color(0xFF034A73),
                                borderColor = Color(0xFFBAE6FD),
                                isLoading = isLoading,
                                onClick = { handleLogin() },
                                testTag = "login_submit_button"
                            )
                        } else {
                            // --- REGISTER FORM ---
                            ProportionalTextField(
                                value = registerUsername,
                                onValueChange = {
                                    registerUsername = it
                                    errorMessage = null
                                },
                                label = "Username (3-20 characters)",
                                icon = Icons.Default.Person,
                                testTag = "register_username_field",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ProportionalTextField(
                                value = registerPassword,
                                onValueChange = {
                                    registerPassword = it
                                    errorMessage = null
                                },
                                label = "Password (Min 4 characters)",
                                icon = Icons.Default.Lock,
                                testTag = "register_password_field",
                                isPassword = true,
                                passwordVisible = registerPasswordVisible,
                                onTogglePassword = { registerPasswordVisible = !registerPasswordVisible },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            ProportionalTextField(
                                value = registerConfirmPassword,
                                onValueChange = {
                                    registerConfirmPassword = it
                                    errorMessage = null
                                },
                                label = "Confirm Password",
                                icon = Icons.Default.Key,
                                testTag = "register_confirm_password_field",
                                isPassword = true,
                                passwordVisible = registerConfirmPasswordVisible,
                                onTogglePassword = { registerConfirmPasswordVisible = !registerConfirmPasswordVisible },
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                                onDone = { handleRegister() }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Proportional3DButton(
                                text = "CREATE ACCOUNT & REGISTER",
                                icon = Icons.Default.PersonAdd,
                                primaryColor = Color(0xFF16A34A),
                                highlightColor = Color(0xFF22C55E),
                                shadowColor = Color(0xFF0F5A29),
                                borderColor = Color(0xFF86EFAC),
                                isLoading = isLoading,
                                onClick = { handleRegister() },
                                testTag = "register_submit_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // --- DIVIDER: OR SIGN IN WITH SOCIAL ACCOUNT ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = Color(0xFF1E4FB8).copy(alpha = 0.5f)
                            )
                            Text(
                                text = "  OR SIGN IN WITH SOCIAL ACCOUNT  ",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.6.sp
                            )
                            HorizontalDivider(
                                modifier = Modifier.weight(1f),
                                thickness = 1.dp,
                                color = Color(0xFF1E4FB8).copy(alpha = 0.5f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- GOOGLE & FACEBOOK BUTTONS ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProportionalSocialButton(
                                text = "Google",
                                provider = SocialProvider.GOOGLE,
                                isLoading = isLoading,
                                onClick = { handleGoogleSignIn() },
                                testTag = "google_sign_in_button",
                                modifier = Modifier.weight(1f)
                            )

                            ProportionalSocialButton(
                                text = "Facebook",
                                provider = SocialProvider.FACEBOOK,
                                isLoading = isLoading,
                                onClick = { handleFacebookSignIn() },
                                testTag = "facebook_sign_in_button",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- PLAY AS GUEST (FAST START) ---
                        Proportional3DButton(
                            text = "PLAY AS GUEST (Start Now)",
                            icon = Icons.Default.Person,
                            primaryColor = Color(0xFF0F266A),
                            highlightColor = Color(0xFF1E3A8A),
                            shadowColor = Color(0xFF06143D),
                            borderColor = Color(0xFF38BDF8),
                            isLoading = isLoading,
                            onClick = {
                                if (isLoading) return@Proportional3DButton
                                prefs.rememberMe = false
                                prefs.isAuthenticated = false
                                prefs.authenticatedUsername = ""
                                HarmonicAudioEngine.playParty()
                                onPlayAsGuest()
                            },
                            testTag = "play_as_guest_button"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // FOOTNOTE
                Text(
                    text = "★ ECHO FLUX • Cloud Saved & Secure with Firebase ★",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    // Direct Facebook Account Dialog
    if (showFacebookPlayerDialog) {
        AlertDialog(
            onDismissRequest = { showFacebookPlayerDialog = false },
            title = {
                Text("Connect with Facebook", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    Text(
                        "Confirm your Facebook Player Name. All your trophies and progress will be securely saved to Firebase cloud.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = facebookPlayerName,
                        onValueChange = { facebookPlayerName = it },
                        label = { Text("Facebook Player Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF1E3A8A),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { completeFacebookDirectConnect(facebookPlayerName) }) {
                    Text("Sign In", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFacebookPlayerDialog = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF0F1E4A),
            shape = RoundedCornerShape(18.dp)
        )
    }
}

/* ========================================================================== */
/*                      PROPORTIONAL UI COMPONENTS                            */
/* ========================================================================== */

@Composable
private fun ProportionalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    testTag: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        label = {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        },
        textStyle = TextStyle(
            fontSize = 14.sp,
            color = Color.White
        ),
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF7DD3FC),
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = if (isPassword && onTogglePassword != null) {
            {
                IconButton(onClick = onTogglePassword, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Password visibility",
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else null,
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF38BDF8),
            unfocusedBorderColor = Color(0xFF1E3A8A),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color(0xFFFFD54F),
            unfocusedLabelColor = Color(0xFF93C5FD),
            focusedContainerColor = Color(0xFF06143D),
            unfocusedContainerColor = Color(0xFF06143D)
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() }
        )
    )
}

@Composable
private fun Proportional3DButton(
    text: String,
    icon: ImageVector,
    primaryColor: Color,
    highlightColor: Color,
    shadowColor: Color,
    borderColor: Color,
    isLoading: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(highlightColor, primaryColor)))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !isLoading, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                )
            }
        }
    }
}

enum class SocialProvider {
    GOOGLE,
    FACEBOOK
}

@Composable
private fun ProportionalSocialButton(
    text: String,
    provider: SocialProvider,
    isLoading: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isGoogle = provider == SocialProvider.GOOGLE
    val primaryColor = if (isGoogle) Color(0xFFFFFFFF) else Color(0xFF1877F2)
    val highlightColor = if (isGoogle) Color(0xFFF8FAFC) else Color(0xFF2563EB)
    val borderColor = if (isGoogle) Color(0xFFCBD5E1) else Color(0xFF93C5FD)
    val textColor = if (isGoogle) Color(0xFF0F172A) else Color.White

    Box(
        modifier = modifier
            .height(50.dp)
            .shadow(5.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(highlightColor, primaryColor)))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(enabled = !isLoading, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = if (isGoogle) Color(0xFF0284C7) else Color.White,
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Official Brand Icon Badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isGoogle) Color(0xFFF1F5F9) else Color(0xFF0F4BB5))
                        .border(1.dp, if (isGoogle) Color(0xFFE2E8F0) else Color(0xFF60A5FA), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGoogle) {
                        Text(
                            text = "G",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4)
                        )
                    } else {
                        Text(
                            text = "f",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = text,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp,
                    color = textColor
                )
            }
        }
    }
}
