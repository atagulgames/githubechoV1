package com.example.ui.auth

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
    val coroutineScope = rememberCoroutineScope()
    val authRepository = remember { AuthRepository(context) }
    val cloudAuthService = remember { RenderAuthAndCloudSaveService.getInstance() }
    val syncManager = remember { CloudSaveSyncManager(context) }
    val prefs = remember { EchoPreferences(context) }

    val hasExistingUser = prefs.authenticatedUsername.isNotBlank() || initialUsername.isNotBlank()
    var selectedTab by remember { mutableStateOf(AuthTab.REGISTER) }

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
                        cloudErr.contains("hatalı", ignoreCase = true) || localErr.contains("şifre", ignoreCase = true) -> {
                            "Kullanıcı adı veya şifre hatalı."
                        }
                        cloudErr.contains("İnternet", ignoreCase = true) && localErr.contains("bulunamadı", ignoreCase = true) -> {
                            "İnternet bağlantısı yok veya sunucuya ulaşılamadı."
                        }
                        localErr.contains("bulunamadı", ignoreCase = true) -> {
                            "Kullanıcı adı veya şifre hatalı."
                        }
                        cloudErr.isNotBlank() -> cloudErr
                        else -> "Kullanıcı adı veya şifre hatalı."
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
            errorMessage = "Kullanıcı adı en fazla 20 karakter olabilir."
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
            errorMessage = "Şifre tekrar alanı şifreyle aynı olmalıdır."
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
                errorMessage = "Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."
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
                successMessage = "Kayıt başarılı! Giriş yapılıyor..."
                onLoginSuccess(derivedUsername, true)
            } else {
                val cloudErr = cloudResult.exceptionOrNull()?.message.orEmpty()
                if (cloudErr.contains("zaten", ignoreCase = true) || cloudErr.contains("already", ignoreCase = true) || cloudErr.contains("kullanılıyor", ignoreCase = true)) {
                    isLoading = false
                    errorMessage = "Bu kullanıcı adı zaten kullanılıyor. Lütfen başka bir kullanıcı adı seçin."
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
                    successMessage = "Kayıt başarılı! Giriş yapılıyor..."
                    onLoginSuccess(localUser.username, true)
                } else {
                    isLoading = false
                    val regErr = localReg.exceptionOrNull()?.message
                    errorMessage = regErr ?: "Kayıt işlemi gerçekleştirilemedi. Lütfen bilgilerinizi kontrol edin."
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

        // 2. Cartoon Game GUI Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // --- TOP CARTOON GAME LOGO & HEADER ---
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .shadow(16.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFFEE58), Color(0xFFFFB300), Color(0xFFF57C00))
                        )
                    )
                    .border(3.5.dp, Color(0xFFFFD54F), CircleShape)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFF0D2F82), Color(0xFF061B50))
                            )
                        )
                        .border(2.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.echo_game_logo),
                        contentDescription = "ECHO Logo",
                        modifier = Modifier.size(54.dp).clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comic 3D Title with 3D drop-shadow text effect
            Box(contentAlignment = Alignment.Center) {
                // Dark Cartoon Drop Shadow Text
                Text(
                    text = "ECHO FLUX",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = Color(0xFF02081C),
                    modifier = Modifier.offset(x = 2.dp, y = 3.dp)
                )
                // Front Bright Comic Golden Text
                Text(
                    text = "ECHO FLUX",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = Color(0xFFFFD54F)
                )
            }

            // Cartoon Arcade Subtitle Pill
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF081C54))
                    .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OYUNCU PORTALI & REZONANS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF7DD3FC)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- CARTOON 3D TAB SWITCHER (KAYIT OL | GİRİŞ YAP) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF06143D))
                    .border(3.dp, Color(0xFF030B24), RoundedCornerShape(22.dp))
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // KAYIT OL TAB (3D Cartoony Emerald Button when active)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
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
                                    Modifier.border(2.dp, Color(0xFF86EFAC), RoundedCornerShape(18.dp))
                                } else Modifier
                            )
                            .clickable {
                                selectedTab = AuthTab.REGISTER
                                errorMessage = null
                                successMessage = null
                            }
                            .padding(vertical = 11.dp)
                            .testTag("tab_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedTab == AuthTab.REGISTER) Color(0xFF14532D) else Color(0xFF0B2160)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = if (selectedTab == AuthTab.REGISTER) Color(0xFFBBF7D0) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KAYIT OL",
                                color = if (selectedTab == AuthTab.REGISTER) Color.White else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    // GİRİŞ YAP TAB (3D Cartoony Azure Button when active)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
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
                                    Modifier.border(2.dp, Color(0xFFBAE6FD), RoundedCornerShape(18.dp))
                                } else Modifier
                            )
                            .clickable {
                                selectedTab = AuthTab.LOGIN
                                errorMessage = null
                                successMessage = null
                            }
                            .padding(vertical = 11.dp)
                            .testTag("tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedTab == AuthTab.LOGIN) Color(0xFF0369A1) else Color(0xFF0B2160)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Login,
                                    contentDescription = null,
                                    tint = if (selectedTab == AuthTab.LOGIN) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GİRİŞ YAP",
                                color = if (selectedTab == AuthTab.LOGIN) Color.White else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- CARTOON GAME POP-UP BOARD / DIALOG PANEL ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(24.dp, RoundedCornerShape(26.dp))
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF0F266A), Color(0xFF0B1F54), Color(0xFF06143D))
                        )
                    )
                    .border(3.5.dp, Color(0xFF051133), RoundedCornerShape(26.dp))
                    .testTag("auth_card")
            ) {
                // Secondary Cartoon Inner Bevel Stroke
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(3.dp)
                        .border(1.5.dp, Color(0xFF1E4FB8).copy(alpha = 0.7f), RoundedCornerShape(23.dp))
                )

                // 4 Corner Metal Rivets / Screws (Classic Cartoon Game GUI Panel Accent)
                Canvas(modifier = Modifier.matchParentSize()) {
                    val rivetRadius = 4.dp.toPx()
                    val margin = 14.dp.toPx()
                    val corners = listOf(
                        Offset(margin, margin),
                        Offset(size.width - margin, margin),
                        Offset(margin, size.height - margin),
                        Offset(size.width - margin, size.height - margin)
                    )
                    corners.forEach { pt ->
                        // Dark rim
                        drawCircle(Color(0xFF030B20), radius = rivetRadius + 1.5.dp.toPx(), center = pt)
                        // Metallic silver center
                        drawCircle(Color(0xFF94A3B8), radius = rivetRadius, center = pt)
                        // Specular shine
                        drawCircle(Color.White, radius = rivetRadius * 0.35f, center = Offset(pt.x - 1.2.dp.toPx(), pt.y - 1.2.dp.toPx()))
                    }
                }

                // Card Body Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating Cartoon Mode Header Ribbon Plate
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (selectedTab == AuthTab.REGISTER) {
                                    Brush.verticalGradient(listOf(Color(0xFF15803D), Color(0xFF166534)))
                                } else {
                                    Brush.verticalGradient(listOf(Color(0xFF0369A1), Color(0xFF075985)))
                                }
                            )
                            .border(
                                2.dp,
                                if (selectedTab == AuthTab.REGISTER) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedTab == AuthTab.REGISTER) "★ YENİ OYUNCU KAYDI ★" else "★ OYUNCU GİRİŞİ ★",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (selectedTab == AuthTab.REGISTER)
                            "Oyuna başlamak için ilk olarak kayıt olmanız zorunludur"
                        else
                            "Kayıtlı hesabınızla giriş yaparak maceraya devam edin",
                        fontSize = 12.sp,
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cartoon Error Banner Plate
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = Brush.verticalGradient(listOf(Color(0xFF991B1B), Color(0xFF7F1D1D)))
                                )
                                .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("login_error_banner")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDC2626)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "!",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = errorMessage.orEmpty(),
                                    color = Color(0xFFFEE2E2),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Cartoon Success Banner Plate
                    if (successMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    brush = Brush.verticalGradient(listOf(Color(0xFF166534), Color(0xFF14532D)))
                                )
                                .border(2.dp, Color(0xFF4ADE80), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
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
                        // --- LOGIN FORM (Kullanıcı Adı ve Şifre) ---
                        // Chunky Cartoon Input: Kullanıcı Adı
                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chunky Cartoon Input: Şifre
                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(10.dp))

                        // Cartoon Game Remember Me Checkbox Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { rememberMe = !rememberMe }
                                .padding(vertical = 4.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                modifier = Modifier.testTag("login_remember_checkbox"),
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF0284C7),
                                    uncheckedColor = Color(0xFF475569),
                                    checkmarkColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Beni Hatırla (Oturumu Sakla)",
                                color = Color(0xFFE2E8F0),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3D CHUNKY CARTOON PUSH BUTTON: GİRİŞ YAP
                        Cartoony3DPushButton(
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to Register Link
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B1F54))
                                .border(1.5.dp, Color(0xFF1E4FB8), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedTab = AuthTab.REGISTER
                                    errorMessage = null
                                    successMessage = null
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Hesabın yok mu? Hemen Kayıt Ol ★",
                                color = Color(0xFFFFD54F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // --- REGISTER FORM (Kullanıcı Adı, Şifre, Şifre Tekrar) ---
                        // Chunky Cartoon Input: Kullanıcı Adı
                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chunky Cartoon Input: Şifre
                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Chunky Cartoon Input: Şifre Tekrar
                        CartoonyGameTextField(
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

                        Spacer(modifier = Modifier.height(18.dp))

                        // 3D CHUNKY CARTOON PUSH BUTTON: KAYIT OL
                        Cartoony3DPushButton(
                            text = "KAYIT OL",
                            icon = Icons.Default.PersonAdd,
                            primaryColor = Color(0xFF16A34A),
                            highlightColor = Color(0xFF22C55E),
                            shadowColor = Color(0xFF0F5A29),
                            borderColor = Color(0xFF86EFAC),
                            isLoading = isLoading,
                            onClick = { handleRegister() },
                            testTag = "register_submit_button"
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch to Login Link
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0B1F54))
                                .border(1.5.dp, Color(0xFF1E4FB8), RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedTab = AuthTab.LOGIN
                                    errorMessage = null
                                    successMessage = null
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Zaten hesabın var mı? Buradan Giriş Yap ★",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Footnote & Cartoon Game Version
            Text(
                text = "★ Her oyuncunun seviye ve skor ilerlemesi bulutta saklanır ★",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "ECHO FLUX • CARTOON ARCADE EDITION",
                color = Color(0xFF64748B),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
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
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF06143D))
            .border(2.5.dp, Color(0xFF1D4594), RoundedCornerShape(16.dp))
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
                    fontWeight = FontWeight.Bold
                )
            },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2C70))
                        .border(1.5.dp, Color(0xFF38BDF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(0xFF7DD3FC),
                        modifier = Modifier.size(17.dp)
                    )
                }
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Şifreyi Göster",
                            tint = Color(0xFFFFD54F)
                        )
                    }
                }
            } else null,
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            shape = RoundedCornerShape(16.dp),
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
    testTag: String
) {
    // 3D tactile game button with 4dp bottom slab depth
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(8.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(shadowColor) // 3D bottom bevel lip
            .testTag(testTag)
            .clickable(enabled = !isLoading, onClick = onClick)
    ) {
        // Top pushable face with top gloss highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(highlightColor, primaryColor)
                    )
                )
                .border(2.dp, borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Subtle top gloss sheen line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.White.copy(alpha = 0.28f))
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
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
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
