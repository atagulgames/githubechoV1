package com.example.ui.auth

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import com.example.data.security.SecureTokenManager
import com.example.ui.menu.drawFlowingCasualGameSymbols
import kotlinx.coroutines.launch

enum class AuthTab {
    LOGIN,
    REGISTER
}

/**
 * Authentication Screen.
 *
 * Fully integrated with Firebase Firestore and Jetpack Credential Manager (Google Sign-In).
 * All player data (trophies, diamonds, coins, tokens, level, stars, skins) are permanently preserved.
 *
 * Implements:
 * - Proportional, non-distorted card layout with balanced spacing and vertical scroll.
 * - Prominent, one-tap Google and Facebook Sign-In buttons.
 * - Fully functional "Remember Me" ("Beni Hatırla") persisting credentials and state.
 * - Pure Firebase Cloud Sync (Zero Render backend dependencies).
 */
@Composable
fun LoginScreen(
    initialUsername: String = "",
    initialRememberMe: Boolean = true,
    onLoginSuccess: (username: String, rememberMe: Boolean) -> Unit,
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

    // Pre-fill username if remembered
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

    // Reset old Render residual tokens
    LaunchedEffect(Unit) {
        HarmonicAudioEngine.playLoginEffect(context)
        SecureTokenManager.clearToken(context)

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
            errorMessage = "Oturum açma ekranı bulunamadı."
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
                    successMessage = "Google ile başarıyla giriş yapıldı! Hoş geldin, $signedInUser"
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
            errorMessage = "Oturum açma ekranı bulunamadı."
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
                    successMessage = "Facebook ile başarıyla giriş yapıldı! Hoş geldin, $signedInUser"
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

    fun handleLogin() {
        if (isLoading) return
        val username = loginUsername.trim()
        val password = loginPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Kullanıcı adı boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Şifre boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            val localResult = authRepository.login(username, password, rememberMe)
            if (localResult.isSuccess) {
                val localUser = localResult.getOrThrow()

                prefs.setAuthenticatedUser(
                    username = localUser.username,
                    remember = rememberMe,
                    passwordHash = localUser.passwordHash,
                    email = localUser.email,
                    fullName = localUser.username
                )
                prefs.rememberMe = rememberMe
                prefs.isAuthenticated = true

                // Cloud sync with Firebase
                syncManager.syncOnLogin(localUser.username)

                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                onLoginSuccess(localUser.username, rememberMe)
            } else {
                isLoading = false
                errorMessage = "Kullanıcı adı veya şifre hatalı."
                HarmonicAudioEngine.playHataSound()
            }
        }
    }

    fun handleRegister() {
        if (isLoading) return
        val username = registerUsername.trim()
        val password = registerPassword.trim()
        val confirmPassword = registerConfirmPassword.trim()

        if (username.isBlank()) {
            errorMessage = "Kullanıcı adı boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (username.length < 3) {
            errorMessage = "Kullanıcı adı en az 3 karakter olmalıdır."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (username.length > 20) {
            errorMessage = "Kullanıcı adı 20 karakterden uzun olamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.isBlank()) {
            errorMessage = "Şifre boş bırakılamaz."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password.length < 4) {
            errorMessage = "Şifre en az 4 karakter olmalıdır."
            HarmonicAudioEngine.playHataSound()
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Girdiğiniz şifreler birbiriyle uyuşmuyor."
            HarmonicAudioEngine.playHataSound()
            return
        }

        isLoading = true
        errorMessage = null
        successMessage = null

        coroutineScope.launch {
            val existingLocal = authRepository.getUserAccount(username)
            if (existingLocal != null) {
                isLoading = false
                errorMessage = "Bu kullanıcı adı zaten alınmış. Lütfen başka bir ad seçin."
                HarmonicAudioEngine.playHataSound()
                return@launch
            }

            val regResult = authRepository.register(
                usernameInput = username,
                emailInput = "",
                passwordInput = password
            )

            if (regResult.isSuccess) {
                val newUser = regResult.getOrThrow()
                prefs.setAuthenticatedUser(
                    username = newUser.username,
                    remember = true,
                    passwordHash = newUser.passwordHash,
                    email = newUser.email,
                    fullName = newUser.username
                )
                prefs.rememberMe = true
                prefs.isAuthenticated = true

                // Cloud register to Firebase Firestore
                syncManager.syncOnLogin(newUser.username)

                isLoading = false
                HarmonicAudioEngine.playLoginEffect(context)
                successMessage = "Kayıt başarılı! Giriş yapılıyor..."
                onLoginSuccess(newUser.username, true)
            } else {
                isLoading = false
                errorMessage = regResult.exceptionOrNull()?.message ?: "Kayıt işlemi tamamlanamadı."
                HarmonicAudioEngine.playHataSound()
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
            .testTag("login_screen_container"),
        contentAlignment = Alignment.Center
    ) {
        // 1. Arcade Animated Game Background
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- LOGO & TITLE HEADER ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
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
                        text = "BULUT SENKRONİZASYONLU HESAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFF7DD3FC)
                    )
                }
            }

            // --- PROPORTIONAL MAIN AUTH CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 420.dp)
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
                    // --- TAB SWITCHER: GİRİŞ YAP | KAYIT OL ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF051133))
                            .border(1.5.dp, Color(0xFF1E3A8A), RoundedCornerShape(14.dp))
                            .padding(4.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // GİRİŞ YAP TAB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
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
                                    .padding(vertical = 10.dp)
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
                                        text = "GİRİŞ YAP",
                                        color = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            // KAYIT OL TAB
                            Box(
                                modifier = Modifier
                                    .weight(1f)
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
                                    .padding(vertical = 10.dp)
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
                                        text = "KAYIT OL",
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
                        // --- GİRİŞ YAP FORMU ---
                        ProportionalTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                errorMessage = null
                            },
                            label = "Kullanıcı Adı",
                            icon = Icons.Default.Person,
                            testTag = "login_username_field",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ProportionalTextField(
                            value = loginPassword,
                            onValueChange = {
                                loginPassword = it
                                errorMessage = null
                            },
                            label = "Şifre",
                            icon = Icons.Default.Lock,
                            testTag = "login_password_field",
                            isPassword = true,
                            passwordVisible = loginPasswordVisible,
                            onTogglePassword = { loginPasswordVisible = !loginPasswordVisible },
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done,
                            onDone = { handleLogin() }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Beni Hatırla (Remember Me)
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
                                text = "Beni Hatırla (Oturumu Açık Tut)",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Proportional3DButton(
                            text = "GİRİŞ YAP",
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
                        // --- KAYIT OL FORMU ---
                        ProportionalTextField(
                            value = registerUsername,
                            onValueChange = {
                                registerUsername = it
                                errorMessage = null
                            },
                            label = "Kullanıcı Adı (3-20 karakter)",
                            icon = Icons.Default.Person,
                            testTag = "register_username_field",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ProportionalTextField(
                            value = registerPassword,
                            onValueChange = {
                                registerPassword = it
                                errorMessage = null
                            },
                            label = "Şifre (En az 4 karakter)",
                            icon = Icons.Default.Lock,
                            testTag = "register_password_field",
                            isPassword = true,
                            passwordVisible = registerPasswordVisible,
                            onTogglePassword = { registerPasswordVisible = !registerPasswordVisible },
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        ProportionalTextField(
                            value = registerConfirmPassword,
                            onValueChange = {
                                registerConfirmPassword = it
                                errorMessage = null
                            },
                            label = "Şifre Tekrar",
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
                            text = "HESAP OLUŞTUR VE KAYDOL",
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- AYIRICI ÇİZGİ: YA DA HIZLI GİRİŞ YAP ---
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
                            text = "  VEYA HIZLI GİRİŞ YAP  ",
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

                    // --- GOOGLE & FACEBOOK BUTONLARI (YAN YANA VE ORANTILI) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // GOOGLE İLE GİRİŞ YAP
                        ProportionalSocialButton(
                            text = "Google",
                            provider = SocialProvider.GOOGLE,
                            isLoading = isLoading,
                            onClick = { handleGoogleSignIn() },
                            testTag = "google_sign_in_button",
                            modifier = Modifier.weight(1f)
                        )

                        // FACEBOOK İLE GİRİŞ YAP
                        ProportionalSocialButton(
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

            Spacer(modifier = Modifier.height(16.dp))

            // FOOTNOTE
            Text(
                text = "★ ECHO FLUX • Tüm İlerlemeniz ve Kupalarınız Firebase ile Güvende ★",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/* ========================================================================== */
/*                      ORANTILI & ESTETİK ARAYÜZ BİLEŞENLERİ                 */
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
                        contentDescription = "Şifre görünürlüğü",
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
            .height(48.dp)
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
                .background(Brush.verticalGradient(listOf(highlightColor, primaryColor)))
                .border(1.5.dp, borderColor, RoundedCornerShape(13.dp)),
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
    val highlightColor = if (isGoogle) Color(0xFFF8FAFC) else Color(0xFF3B82F6)
    val shadowColor = if (isGoogle) Color(0xFF94A3B8) else Color(0xFF0C4A9E)
    val borderColor = if (isGoogle) Color(0xFFCBD5E1) else Color(0xFF93C5FD)
    val textColor = if (isGoogle) Color(0xFF1E293B) else Color.White

    Box(
        modifier = modifier
            .height(44.dp)
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
                .background(Brush.verticalGradient(listOf(highlightColor, primaryColor)))
                .border(1.5.dp, borderColor, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
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
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isGoogle) Color(0xFFF1F5F9) else Color(0xFF0F4BB5))
                            .border(1.dp, if (isGoogle) Color(0xFFE2E8F0) else Color(0xFF60A5FA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isGoogle) {
                            Text(
                                text = "G",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4285F4)
                            )
                        } else {
                            Text(
                                text = "f",
                                fontSize = 16.sp,
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
}
