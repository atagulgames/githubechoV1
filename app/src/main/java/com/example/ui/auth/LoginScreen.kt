package com.example.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.data.CloudSaveSyncManager
import com.example.data.EchoPreferences
import com.example.data.FirebaseAuthManager
import com.example.data.RenderAuthAndCloudSaveService
import com.example.data.security.SecureTokenManager
import kotlinx.coroutines.Dispatchers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.SportsEsports
import com.example.ui.menu.drawFlowingCasualGameSymbols
import com.example.R
import com.example.audio.HarmonicAudioEngine
import com.example.data.AuthRepository
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN,
    REGISTER
}

/**
 * Authentication & Registration Screen.
 * 
 * Strict constraints:
 * - No guest login or skip button. All user entries are saved to the persistent database.
 * - Each user account strictly preserves their own individual progress (level, stars, tokens, theme).
 * - Full Registration ("Kayıt Ol") and Login ("Giriş Yap") menus.
 */
@Composable
fun LoginScreen(
    initialUsername: String = "",
    initialRememberMe: Boolean = false,
    onLoginSuccess: (username: String, rememberMe: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    val cloudAuthService = remember { RenderAuthAndCloudSaveService.getInstance() }
    val syncManager = remember { CloudSaveSyncManager(context) }
    val prefs = remember { EchoPreferences(context) }
    val firebaseAuthManager = remember { FirebaseAuthManager(context) }

    val hasExistingUser = prefs.authenticatedUsername.isNotBlank() || initialUsername.isNotBlank()
    var selectedTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Login Fields (Kullanıcı Adı ve Şifre) - Varsayılan hesap verilmez
    var loginUsername by remember { mutableStateOf(if (prefs.rememberMe && prefs.isAuthenticated) prefs.authenticatedUsername else "") }
    var loginPassword by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(initialRememberMe) }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register Fields (Kullanıcı Adı, Şifre, Şifre Tekrar)
    var registerUsername by remember { mutableStateOf("") }
    var registerPassword by remember { mutableStateOf("") }
    var registerConfirmPassword by remember { mutableStateOf("") }
    var registerPasswordVisible by remember { mutableStateOf(false) }
    var registerConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Feedback State
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // Play login sound effect on enter
    LaunchedEffect(Unit) {
        HarmonicAudioEngine.playLoginEffect(context)
        if (prefs.rememberMe && prefs.isAuthenticated && firebaseAuthManager.isUserSignedIn) {
            firebaseAuthManager.attemptSilentGoogleSignIn(
                scope = coroutineScope,
                onSuccess = { signedInUser ->
                    onLoginSuccess(signedInUser, true)
                },
                onUnauthenticated = { /* Keep on screen */ }
            )
        }
    }

    fun handleGoogleSignIn() {
        if (isLoading) return
        val currentActivity = activity
        if (currentActivity == null) {
            errorMessage = "Sign-in activity could not be found."
            return
        }
        isLoading = true
        errorMessage = null
        successMessage = null

        firebaseAuthManager.signInWithGoogle(
            activity = currentActivity,
            scope = coroutineScope,
            onSuccess = { signedInUser ->
                isLoading = false
                successMessage = "Signed in with Google! Welcome, $signedInUser"
                HarmonicAudioEngine.playParty()
                onLoginSuccess(signedInUser, rememberMe)
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
            errorMessage = "Sign-in activity could not be found."
            return
        }
        isLoading = true
        errorMessage = null
        successMessage = null

        firebaseAuthManager.signInWithFacebook(
            activity = currentActivity,
            scope = coroutineScope,
            onSuccess = { signedInUser ->
                isLoading = false
                successMessage = "Signed in with Facebook! Welcome, $signedInUser"
                HarmonicAudioEngine.playParty()
                onLoginSuccess(signedInUser, rememberMe)
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

    fun handleLogin() {
        if (isLoading) return
        val username = loginUsername.trim()
        val password = loginPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Username cannot be empty."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Password cannot be empty."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            // 1. First attempt login via Render Cloud Backend
            val cloudResult = cloudAuthService.login(username, password)
            if (cloudResult.isSuccess) {
                val (user, token) = cloudResult.getOrThrow()
                SecureTokenManager.saveToken(context, token)

                val derivedUsername = if (user.fullName.isNotBlank()) user.fullName else username
                prefs.setAuthenticatedUser(
                    username = derivedUsername,
                    remember = rememberMe,
                    passwordHash = "",
                    email = user.email,
                    fullName = user.fullName
                )

                // Sync with local account cache with password
                val existingAcc = authRepository.getUserAccount(derivedUsername)
                if (existingAcc == null) {
                    authRepository.register(
                        usernameInput = derivedUsername,
                        emailInput = user.email,
                        passwordInput = password
                    )
                } else {
                    authRepository.registerOrUpdateExternalUser(
                        username = derivedUsername,
                        email = user.email,
                        fullName = user.fullName
                    )
                }

                // Synchronize Cloud Save
                syncManager.syncOnLogin(token)

                // Also update score on Render leaderboard only if player has earned trophies (> 0)
                if (prefs.trophies > 0) {
                    coroutineScope.launch(Dispatchers.IO) {
                        try {
                            com.example.data.RenderLeaderboardService.getInstance().submitScore(derivedUsername, prefs.trophies)
                        } catch (_: Exception) {}
                    }
                }

                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                onLoginSuccess(derivedUsername, rememberMe)
            } else {
                // 2. Fallback to local persistent auth (offline support or local user)
                val localResult = authRepository.login(username, password, rememberMe)
                if (localResult.isSuccess) {
                    val localUser = localResult.getOrThrow()
                    val token = SecureTokenManager.getToken(context) ?: "render_jwt_${System.currentTimeMillis()}"
                    SecureTokenManager.saveToken(context, token)

                    prefs.setAuthenticatedUser(
                        username = localUser.username,
                        remember = rememberMe,
                        passwordHash = localUser.passwordHash,
                        email = localUser.email,
                        fullName = localUser.username
                    )

                    if (prefs.trophies > 0) {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                com.example.data.RenderLeaderboardService.getInstance().submitScore(localUser.username, prefs.trophies)
                            } catch (_: Exception) {}
                        }
                    }

                    isLoading = false
                    HarmonicAudioEngine.playLoginEffect(context)
                    onLoginSuccess(localUser.username, rememberMe)
                } else {
                    isLoading = false
                    val cloudErr = cloudResult.exceptionOrNull()?.message.orEmpty()
                    val localErr = localResult.exceptionOrNull()?.message.orEmpty()

                    errorMessage = when {
                        cloudErr.contains("hatalı", ignoreCase = true) || localErr.contains("şifre", ignoreCase = true) ||
                        cloudErr.contains("invalid", ignoreCase = true) || localErr.contains("invalid", ignoreCase = true) -> {
                            "Incorrect username or password."
                        }
                        cloudErr.contains("İnternet", ignoreCase = true) || cloudErr.contains("connection", ignoreCase = true) -> {
                            "No internet connection or server unreachable."
                        }
                        localErr.contains("bulunamadı", ignoreCase = true) || localErr.contains("not found", ignoreCase = true) -> {
                            "Incorrect username or password."
                        }
                        cloudErr.isNotBlank() -> cloudErr
                        else -> "Incorrect username or password."
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
            // Check local account first
            val existingLocal = authRepository.getUserAccount(username)
            if (existingLocal != null) {
                isLoading = false
                errorMessage = "This username is already taken. Please choose another."
                HarmonicAudioEngine.playHataSound()
                return@launch
            }

            // Register on Render Backend
            val cloudResult = cloudAuthService.register(username = username, email = "", password = password)
            if (cloudResult.isSuccess) {
                val (user, token) = cloudResult.getOrThrow()
                SecureTokenManager.saveToken(context, token)

                val derivedUsername = if (user.fullName.isNotBlank()) user.fullName else username
                prefs.setAuthenticatedUser(
                    username = derivedUsername,
                    remember = true,
                    passwordHash = "",
                    email = user.email,
                    fullName = user.fullName
                )

                // Save to local Room & Vault with secure password hash
                authRepository.register(
                    usernameInput = derivedUsername,
                    emailInput = user.email,
                    passwordInput = password
                )

                // Initial Cloud Save synchronization
                syncManager.syncOnLogin(token)

                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                successMessage = "Registration successful! Signing in..."
                onLoginSuccess(derivedUsername, true)
            } else {
                val cloudErr = cloudResult.exceptionOrNull()?.message.orEmpty()
                if (cloudErr.contains("zaten", ignoreCase = true) || cloudErr.contains("already", ignoreCase = true) || cloudErr.contains("kullanılıyor", ignoreCase = true)) {
                    isLoading = false
                    errorMessage = "This username is already taken. Please choose another."
                    HarmonicAudioEngine.playHataSound()
                    return@launch
                }

                // Cloud returned error/offline -> Fallback to robust local persistent registration
                val localReg = authRepository.register(
                    usernameInput = username,
                    emailInput = "",
                    passwordInput = password
                )
                if (localReg.isSuccess) {
                    val localUser = localReg.getOrThrow()
                    val token = "render_jwt_${System.currentTimeMillis()}"
                    SecureTokenManager.saveToken(context, token)

                    prefs.setAuthenticatedUser(
                        username = localUser.username,
                        remember = true,
                        passwordHash = localUser.passwordHash,
                        email = localUser.email,
                        fullName = localUser.username
                    )

                    isLoading = false
                    HarmonicAudioEngine.playLoginEffect(context)
                    successMessage = "Registration successful! Signing in..."
                    onLoginSuccess(localUser.username, true)
                } else {
                    isLoading = false
                    val regErr = localReg.exceptionOrNull()?.message
                    errorMessage = regErr ?: "Registration could not be completed. Please check your credentials."
                    HarmonicAudioEngine.playHataSound()
                }
            }
        }
    }

    // Cartoon Game UI background animations
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
            .testTag("login_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Vibrant Royal Arcade Game Background with Slow Flowing Casual Game Watermarks
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
            // Ambient Radial Spotlight in the center
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E4FB8).copy(alpha = 0.35f), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.7f
                ),
                center = center,
                radius = size.minDimension * 0.7f
            )
            // Animated flowing cartoon game symbols (Trophies, Crowns, Stars, Gamepads, Gems, Rockets, Coins)
            drawFlowingCasualGameSymbols(
                canvasSize = size,
                flowProgress = flowProgress,
                pulseProgress = pulseProgress
            )
        }

        // 2. Cartoon Game GUI Content (Compact, fitting perfectly on screen)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- TOP COMPACT LOGO & HEADER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                // Mini 3D Logo Badge
                Box(
                    modifier = Modifier
                        .size(38.dp)
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

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    // Comic 3D Title
                    Box {
                        Text(
                            text = "ECHO FLUX",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color(0xFF02081C),
                            modifier = Modifier.offset(x = 1.5.dp, y = 2.dp)
                        )
                        Text(
                            text = "ECHO FLUX",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp,
                            color = Color(0xFFFFD54F)
                        )
                    }
                    Text(
                        text = "LOGIN / SIGN IN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7DD3FC)
                    )
                }
            }

            // --- CARTOON 3D TAB SWITCHER (SIGN IN | SIGN UP) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF06143D))
                    .border(2.dp, Color(0xFF030B24), RoundedCornerShape(16.dp))
                    .padding(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // SIGN IN TAB (Active by default)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedTab == AuthTab.LOGIN) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            )
                            .then(
                                if (selectedTab == AuthTab.LOGIN) {
                                    Modifier.border(2.dp, Color(0xFFBAE6FD), RoundedCornerShape(14.dp))
                                } else Modifier
                            )
                            .clickable {
                                selectedTab = AuthTab.LOGIN
                                errorMessage = null
                                successMessage = null
                            }
                            .padding(vertical = 7.dp)
                            .testTag("tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = if (selectedTab == AuthTab.LOGIN) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIGN IN",
                                color = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // SIGN UP TAB
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedTab == AuthTab.REGISTER) {
                                    Brush.verticalGradient(
                                        listOf(Color(0xFF22C55E), Color(0xFF16A34A))
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                            )
                            .then(
                                if (selectedTab == AuthTab.REGISTER) {
                                    Modifier.border(2.dp, Color(0xFF86EFAC), RoundedCornerShape(14.dp))
                                } else Modifier
                            )
                            .clickable {
                                selectedTab = AuthTab.REGISTER
                                errorMessage = null
                                successMessage = null
                            }
                            .padding(vertical = 7.dp)
                            .testTag("tab_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = if (selectedTab == AuthTab.REGISTER) Color(0xFFBBF7D0) else Color(0xFF94A3B8),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SIGN UP",
                                color = if (selectedTab == AuthTab.REGISTER) Color.White else Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- CARTOON GAME POP-UP BOARD / DIALOG PANEL ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF0F266A), Color(0xFF0B1F54), Color(0xFF06143D))
                        )
                    )
                    .border(2.5.dp, Color(0xFF051133), RoundedCornerShape(22.dp))
                    .testTag("auth_card")
            ) {
                // Secondary Inner Bevel Stroke
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(2.5.dp)
                        .border(1.dp, Color(0xFF1E4FB8).copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                )

                // Card Body Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating Mode Header Ribbon Plate
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (selectedTab == AuthTab.REGISTER) {
                                    Brush.verticalGradient(listOf(Color(0xFF15803D), Color(0xFF166534)))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFF0369A1), Color(0xFF075985)))
                                }
                            )
                            .border(
                                1.5.dp,
                                if (selectedTab == AuthTab.REGISTER) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(vertical = 5.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == AuthTab.REGISTER) "★ CREATE ACCOUNT ★" else "★ PLAYER SIGN IN ★",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Error Banner Plate
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.verticalGradient(listOf(Color(0xFF991B1B), Color(0xFF7F1D1D)))
                                )
                                .border(1.5.dp, Color(0xFFEF4444), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("login_error_banner")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "!",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFEE2E2),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Success Banner Plate
                    if (successMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    brush = Brush.verticalGradient(listOf(Color(0xFF166534), Color(0xFF14532D)))
                                )
                                .border(1.5.dp, Color(0xFF4ADE80), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = successMessage.orEmpty(),
                                    color = Color(0xFFDCFCE7),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (selectedTab == AuthTab.LOGIN) {
                        // --- LOGIN FORM ---
                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(6.dp))

                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Remember Me Checkbox Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 2.dp, horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                modifier = Modifier.testTag("login_remember_checkbox").size(28.dp),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF0284C7),
                                    uncheckedColor = Color(0xFF475569),
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Remember Me (Keep Signed In)",
                                color = Color(0xFFE2E8F0),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // SIGN IN BUTTON
                        Cartoony3DPushButton(
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

                        Spacer(modifier = Modifier.height(4.dp))

                        // Switch to Register Link
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedTab = AuthTab.REGISTER
                                    errorMessage = null
                                    successMessage = null
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Don't have an account? Sign Up ★",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // --- REGISTER FORM ---
                        CartoonyGameTextField(
                            value = registerUsername,
                            onValueChange = {
                                registerUsername = it
                                errorMessage = null
                            },
                            label = "Username (3-20 chars)",
                            icon = Icons.Default.Person,
                            testTag = "register_username_field",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        CartoonyGameTextField(
                            value = registerPassword,
                            onValueChange = {
                                registerPassword = it
                                errorMessage = null
                            },
                            label = "Password (Min 4 chars)",
                            icon = Icons.Default.Lock,
                            testTag = "register_password_field",
                            isPassword = true,
                            passwordVisible = registerPasswordVisible,
                            onTogglePassword = { registerPasswordVisible = !registerPasswordVisible },
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // SIGN UP BUTTON
                        Cartoony3DPushButton(
                            text = "SIGN UP",
                            icon = Icons.Default.PersonAdd,
                            primaryColor = Color(0xFF16A34A),
                            highlightColor = Color(0xFF22C55E),
                            shadowColor = Color(0xFF0F5A29),
                            borderColor = Color(0xFF86EFAC),
                            isLoading = isLoading,
                            onClick = { handleRegister() },
                            testTag = "register_submit_button"
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Switch to Login Link
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedTab = AuthTab.LOGIN
                                    errorMessage = null
                                    successMessage = null
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Already have an account? Sign In ★",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // --- FIREBASE SOCIAL AUTHENTICATION (SIDE-BY-SIDE IN ONE ROW) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = Color(0xFF1E4FB8).copy(alpha = 0.4f)
                        )
                        Text(
                            text = "  OR QUICK SIGN IN  ",
                            color = Color(0xFF94A3B8),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = Color(0xFF1E4FB8).copy(alpha = 0.4f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // GOOGLE SIGN IN
                        SocialCartoony3DButton(
                            text = "Google",
                            provider = SocialProvider.GOOGLE,
                            isLoading = isLoading,
                            onClick = { handleGoogleSignIn() },
                            testTag = "google_sign_in_button",
                            modifier = Modifier.weight(1f)
                        )

                        // FACEBOOK SIGN IN
                        SocialCartoony3DButton(
                            text = "Facebook",
                            provider = SocialProvider.FACEBOOK,
                            isLoading = isLoading,
                            onClick = { handleFacebookSignIn() },
                            testTag = "facebook_sign_in_button",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer Footnote
            Text(
                text = "★ ECHO FLUX • Cloud Synchronized Account ★",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/* ========================================================================== */
/*                      CARTOON GAME GUI COMPONENT HELPERS                    */
/* ========================================================================== */

@Composable
private fun CartoonyGameTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    testTag: String,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Default,
    onDone: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF06143D))
            .border(1.5.dp, Color(0xFF1D4594), RoundedCornerShape(12.dp))
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
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 13.sp,
                color = Color.White
            ),
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2C70))
                        .border(1.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF7DD3FC),
                        modifier = Modifier.size(14.dp)
                    )
                }
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            } else null,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedLabelColor = Color(0xFFFFD54F),
                unfocusedLabelColor = Color(0xFF93C5FD),
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
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
}

@Composable
private fun Cartoony3DPushButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            .height(44.dp)
            .shadow(6.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(shadowColor)
            .testTag(testTag)
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(highlightColor, primaryColor)
                    )
                )
                .border(1.5.dp, borderColor, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp))
                    .background(Color.White.copy(alpha = 0.28f))
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}

enum class SocialProvider {
    GOOGLE,
    FACEBOOK
}

@Composable
private fun SocialCartoony3DButton(
    text: String,
    provider: SocialProvider,
    isLoading: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val isGoogle = provider == SocialProvider.GOOGLE
    val primaryColor = if (isGoogle) Color(0xFFFFFFFF) else Color(0xFF1877F2)
    val highlightColor = if (isGoogle) Color(0xFFF8FAFC) else Color(0xFF3B82F6)
    val shadowColor = if (isGoogle) Color(0xFF94A3B8) else Color(0xFF0C4A9E)
    val borderColor = if (isGoogle) Color(0xFFCBD5E1) else Color(0xFF93C5FD)
    val textColor = if (isGoogle) Color(0xFF1E293B) else Color.White

    Box(
        modifier = modifier
            .height(42.dp)
            .shadow(5.dp, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(shadowColor)
            .testTag(testTag)
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(highlightColor, primaryColor)
                    )
                )
                .border(1.5.dp, borderColor, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp))
                    .background(Color.White.copy(alpha = if (isGoogle) 0.5f else 0.28f))
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = if (isGoogle) Color(0xFF0284C7) else Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isGoogle) Color(0xFFF1F5F9) else Color(0xFF0F4BB5))
                            .border(1.dp, if (isGoogle) Color(0xFFE2E8F0) else Color(0xFF60A5FA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGoogle) {
                            Text(
                                text = "G",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4)
                            )
                        } else {
                            Text(
                                text = "f",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

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
}
